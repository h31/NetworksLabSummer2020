import algebras.CrawlerCache.UriNode
import algebras.{AsyncDownloader, CrawlerCache}
import cats.effect.{Concurrent, ConcurrentEffect, Timer}
import config.Config
import fs2._
import http.HttpApi.HttpApi
import org.http4s.server.blaze.BlazeServerBuilder

trait Server[F[_]] {
  def start: HttpApi[F] => Stream[F, Unit]
}

object Server {

  def blazeHttp[F[_]: Timer: ConcurrentEffect](cfg: Config): Server[F] =
    new Server[F] {
      def start: HttpApi[F] => Stream[F, Unit] =
        (api: HttpApi[F]) =>
          for {
            downloader <- Stream.resource(AsyncDownloader.make[F])
            cache      <- Stream.eval(CrawlerCache.acquireOne[F](???))
            server = cfg.serverConfig
            _ <- BlazeServerBuilder[F]
                  .bindHttp(server.port, server.host)
                  .withHttpApp(api.httpApp)
                  .serve
          } yield ()
    }

  def nativeHttp[F[_]: Concurrent](cfg: Config): Server[F] = ???
}
