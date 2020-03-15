import cats.syntax.functor._
import cats.syntax.apply._
import cats.effect.{ExitCode, IO, IOApp}
import fs2._
import http.HttpApi
import org.slf4j.LoggerFactory

object App extends IOApp {
  implicit val logger = LoggerFactory.getLogger(getClass)
  val cfg             = Server.ServerConfig(8080)

  type RunnerFn[F[_]] = F[(Server[F], HttpApi[F])] => F[ExitCode]

  def runner: RunnerFn[IO] =
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
    runner(
        (Server.blazeHttp(cfg), HttpApi[IO]).tupled
    )
}
