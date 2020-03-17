import algebras.AsyncDownloader
import cats.syntax.functor._
import cats.syntax.apply._
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import fs2._
import http.HttpApi
import org.http4s.Uri
import org.slf4j.LoggerFactory

object App extends IOApp {
  implicit val logger = LoggerFactory.getLogger(getClass)
  val cfg             = Server.ServerConfig(8080)

  type Runner[F[_]] = F[(Server[F], HttpApi[F])] => F[ExitCode]

  def runner: Runner[IO] =
    serverImpl =>
      Stream
        .eval(
            serverImpl
        )
        .flatMap { case (server, api) => server.start(api) }
        .concurrently(Stream.eval(IO(logger.info(s"server started on port: ${cfg.port}"))))
        .compile
        .drain
        .as(ExitCode.Success)

  def run(args: List[String]): IO[ExitCode] =
    runner {
      (Server.blazeHttp(cfg), HttpApi[IO]).tupled
    }
}

object Test extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    (for {
      d <- AsyncDownloader.make[IO]
      _ <- d.use { client =>
              client.fetchPage(Uri.fromString("https://yandex.ru/").getOrElse(Uri.unsafeFromString("")))
            }
            .map(println(_))
    } yield ExitCode.Success)
}
