import cats.syntax.functor._
import cats.effect.{ExitCode, IO, IOApp}
import fs2._
import org.slf4j.LoggerFactory

object App extends IOApp {
  implicit val logger = LoggerFactory.getLogger(getClass)
  val cfg             = Server.ServerConfig(8080)

  def run(args: List[String]): IO[ExitCode] =
    Stream
      .eval(
          Server.blazeHttp[IO](cfg)
      )
      .flatMap(_.start)
      .concurrently(Stream.eval(IO(logger.info(s"server started on port: ${cfg.port}"))))
      .compile
      .drain
      .as(ExitCode.Success)
}
