package native

import algebras.{CrawlerFileSystem, FileSystem}
import cats.effect.Sync
import native.HttpSocket.HttpMessage
import cats.syntax.flatMap._
import cats.syntax.functor._
import CrawlerFileSystem._

import scala.util.matching.Regex

// hardcoded
object NaiveMessageAnalyzer {
  private val pattern: Regex = "^\\/$|(\\/[a-zA-Z_0-9-.]+)+$".r
  private val ok             = "200 OK"
  private val nf             = "404 Not Found"
  private val GET            = "GET"

  private[NaiveMessageAnalyzer] case class Headers(values: String)

  private val headers: Int => String => Headers =
    contentLength =>
      status =>
        Headers(
            s"HTTP/1.1 $status\r\n" +
            "Server: Pashnik\r\n" +
            s"Content-Length: $contentLength\r\n" +
            "\r\n"
        )

  def analyze[F[_]: Sync](fs: FileSystem[F])(
        httpMessage: HttpMessage
  ): F[HttpMessage] = {
    val ifError = fs.readFile(errorPagePath).map(_ -> nf)

    (if (httpMessage.body.contains(GET)) {
       pattern.findFirstIn(httpMessage.body.split(" ")(1)) match {
         case Some(value) =>
           val name =
             if (value == "/") indexPagePath.getName
             else value.tail

           fs.scan(_.getName == name)
             .flatMap {
               case Nil         => ifError
               case ::(head, _) => fs.readFile(head).map(_ -> ok)
             }
         case None =>
           fs.readFile(errorPagePath).map(_ -> nf)
       }
     } else ifError).map {
      case (htmlContent, status) =>
        HttpMessage(
            headers(htmlContent.value.length)(status).values +
            htmlContent.value
        )
    }
  }
}
