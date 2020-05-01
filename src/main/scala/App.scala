import algebras.AsyncDownloader
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.syntax.functor._
import http.HttpApi
import http.HttpApi.HttpApi
import org.http4s.Uri
import org.slf4j.LoggerFactory
import io.circe.generic.auto._
import io.circe.syntax._
import fs2._

object App extends IOApp {
  implicit val logger = LoggerFactory.getLogger(getClass)
  val cfg             = Server.ServerConfig(8080)

  type Runner[F[_]] = ((Server[F], HttpApi[F])) => Stream[F, Unit]

  def runner: Runner[IO] =
    serverImpl =>
      Stream
        .emit(serverImpl)
        .flatMap { case (server, api) => server.start(api) }
        .concurrently(Stream.eval(IO(logger.info(s"server started with config:\n ${cfg.asJson}"))))

  def run(args: List[String]): IO[ExitCode] =
    Stream
      .resource(Blocker[IO])
      .flatMap { implicit blocker =>
        runner { Server.blazeHttp(cfg) -> HttpApi.V1.api }
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
                  Uri.fromString("https://football.kulichki.net").getOrElse(Uri.unsafeFromString(""))
              )
              .map(println(_))
          )
    } yield ()).compile.drain.as(ExitCode.Success)
}
