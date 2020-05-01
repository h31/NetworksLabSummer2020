import algebras.AsyncDownloader
import cats.effect.{Concurrent, ConcurrentEffect, Timer}
import fs2._
import http.HttpApi.HttpApi
import org.http4s.server.blaze.BlazeServerBuilder

trait Server[F[_]] {
  def start: HttpApi[F] => Stream[F, Unit]
}

object Server {
  case class ServerConfig(port: Int)

  def blazeHttp[F[_]: Timer: ConcurrentEffect](cfg: ServerConfig): Server[F] =
    new Server[F] {
      def start: HttpApi[F] => Stream[F, Unit] =
        (api: HttpApi[F]) =>
          for {
            downloader <- Stream.resource(AsyncDownloader.make[F])
            _ <- BlazeServerBuilder[F]
                  .withHttpApp(api.httpApp)
                  .serve
          } yield ()
    }

  def nativeHttp[F[_]: Concurrent](cfg: ServerConfig): Server[F] = ???
}
