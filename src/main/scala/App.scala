import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.syntax.functor._
import config.Config
import org.slf4j.LoggerFactory
import fs2._

object App extends IOApp {
  implicit val logger = LoggerFactory.getLogger(getClass)

  def run(args: List[String]): IO[ExitCode] =
    Stream
      .resource(Blocker[IO])
      .flatMap { implicit blocker =>
        for {
          config <- Stream.eval(Config.load[IO])
          _      <- Server.blazeHttp(config).start
        } yield ()
      }
      .compile
      .drain
      .as(ExitCode.Success)
}
