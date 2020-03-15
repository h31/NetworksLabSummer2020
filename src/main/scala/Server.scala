import cats.effect.{Concurrent, ConcurrentEffect, Timer}
import fs2._
import http.HttpApi
import org.http4s.server.blaze.BlazeServerBuilder

trait Server[F[_]] {
  def start(api: HttpApi[F]): Stream[F, Unit]
}

object Server {
  case class ServerConfig(port: Int)

  def blazeHttp[F[_]: Timer](cfg: ServerConfig)(implicit C: ConcurrentEffect[F]): F[Server[F]] = C.delay {
    api: HttpApi[F] =>
      for {
        _ <- BlazeServerBuilder[F]
              .withHttpApp(api.httpAppWithLg)
              .serve
      } yield ()
  }

  def nativeHttp[F[_]](cfg: ServerConfig)(implicit C: Concurrent[F]): F[Server[F]] = C.delay { api: HttpApi[F] =>
    ???
  }
}
