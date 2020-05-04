package programs

import algebras.{Downloader, FileSystem}
import algebras.Extractor.{CssUri, HtmlResource, ImgUri, JsUri, LinkUri}
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect.{Concurrent, Timer}
import domain.page.HtmlContent
import fs2._
import org.http4s.Uri
import org.slf4j.Logger
import algebras.Extractor.syntax._
import fs2.concurrent.Queue
import utils.XmlTraversable

trait Crawler[F[_]] {
  def crawl: Stream[F, Unit]
}

object Crawler {
  case class Start(uri: Uri)
  def apply[F[_]: Concurrent: Timer](fetcher: Downloader[F], fs: FileSystem[F])(
        startUri: Start
  )(implicit L: Logger): Crawler[F] =
    new Crawler[F] {
      type Index = Queue[F, LinkUri]

      def crawl: Stream[F, Unit] =
        Stream
          .eval(Queue.unbounded[F, LinkUri])
          .flatMap { index =>
            Stream.eval(
                index
                .enqueue1(LinkUri(startUri.uri))
            ) ++
              index.dequeue
                .map(start(index, _))
                .parJoinUnbounded
          }

      def fetch(link: HtmlResource)(continue: HtmlContent => Stream[F, Unit]): Stream[F, Unit] =
        Stream
          .eval(fetcher.fetchPage(link.uri))
          .flatMap(_.fold(Stream.emit(()).covary[F])(continue))

      def start(index: Index, currentLink: HtmlResource): Stream[F, Unit] =
        fetch(currentLink) { content =>
          val pipeline =
            for {
              resources <- Stream.eval(extract(content))
              _ <- Stream
                    .emits(resources)
                    .collect { case l: LinkUri => l }
                    .through(index.enqueue)
              updatedContent <- Stream.eval(XmlTraversable.modify[F](content)(resources))
            } yield updatedContent -> resources

          pipeline.flatMap {
            case (content, resources) =>
              fs.writeFile(currentLink.toPath, content) ++
                Stream
                  .emits(resources)
                  .covary[F]
                  .collect {
                    case r @ (_: JsUri | _: CssUri | _: ImgUri) => r
                  }
                  .flatMap(r => fetch(r)(fs.writeFile(r.toPath, _)))
          }
        }

      val extract: HtmlContent => F[List[HtmlResource]] =
        html =>
          for {
            styles  <- html.extract[F, CssUri]
            scripts <- html.extract[F, JsUri]
            links   <- html.extract[F, LinkUri]
            imgs    <- html.extract[F, ImgUri]
          } yield (styles ++ scripts ++ links ++ imgs).distinct
            .collect { case res: HtmlResource if res.uri.host.isDefined && res.uri.scheme.isDefined => res }
    }
}
