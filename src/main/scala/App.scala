import algebras.AsyncDownloader
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.syntax.functor._
import config.Config
import http.HttpApi
import http.HttpApi.HttpApi
import org.http4s.Uri
import org.slf4j.LoggerFactory
import fs2._

object App extends IOApp {
  implicit val logger = LoggerFactory.getLogger(getClass)

  type Runner[F[_]] = ((Server[F], HttpApi[F])) => Stream[F, Unit]

  def runner: Runner[IO] =
    serverImpl =>
      Stream
        .emit(serverImpl)
        .flatMap { case (server, api) => server.start(api) }
        .concurrently(Stream.eval(IO(logger.info(s"server started"))))

  def run(args: List[String]): IO[ExitCode] =
    Stream
      .resource(Blocker[IO])
      .flatMap { implicit blocker =>
        for {
          config <- Stream.eval(Config.load[IO])
          _      <- runner { Server.blazeHttp(config) -> HttpApi.V1.api }
        } yield ()
      }
      .compile
      .drain
      .as(ExitCode.Success)
}

object Test extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    (for {
      client <- Stream.resource(AsyncDownloader.make[IO])
      _ <- Stream.eval(
              client
              .fetchPage(
                  Uri.fromString("https://github.com/pureconfig/pureconfig").getOrElse(Uri.unsafeFromString(""))
              )
              .map(println(_))
          )
    } yield ()).compile.drain.as(ExitCode.Success)
}
