import cats.effect.{Concurrent, ConcurrentEffect, Timer}
import fs2._
import http.HttpApi
import org.http4s.server.blaze.BlazeServerBuilder

trait Server[F[_]] {
  def start: Stream[F, Unit]
}

object Server {
  case class ServerConfig(port: Int)

  def blazeHttp[F[_]: Timer](cfg: ServerConfig)(implicit C: ConcurrentEffect[F]): F[Server[F]] = C.delay {
    new Server[F] {
      def start: Stream[F, Unit] =
        for {
          api <- Stream.eval(HttpApi[F])
          _ <- BlazeServerBuilder[F]
                .withHttpApp(api.httpAppWithLg)
                .serve
        } yield ()
    }
  }

  def myHttp[F[_]](cfg: ServerConfig)(implicit C: Concurrent[F]): F[Server[F]] = C.delay {
    new Server[F] {
      override def start: Stream[F, Unit] = ???
    }
  }
}
