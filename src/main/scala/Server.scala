import algebras.CrawlerCache.UriNode
import algebras.{AsyncDownloader, CrawlerCache, CrawlerFileSystem}
import cats.effect.{Blocker, Concurrent, ConcurrentEffect, ContextShift, Timer}
import config.Config
import fs2._
import http.HttpApi.HttpApi
import org.http4s.Uri
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.Logger
import programs.Crawler
import programs.Crawler.Start

trait Server[F[_]] {
  def start: HttpApi[F] => Stream[F, Unit]
}

object Server {

  def blazeHttp[F[_]: Timer: ConcurrentEffect](
        cfg: Config
  )(implicit C: ContextShift[F], B: Blocker, L: Logger): Server[F] =
    new Server[F] {
      def start: HttpApi[F] => Stream[F, Unit] =
        (api: HttpApi[F]) =>
          for {
            downloader <- Stream.resource(AsyncDownloader.make[F])
            fileSystem <- Stream.eval(CrawlerFileSystem[F])
            _ <- Crawler[F](
                    downloader
                  , fileSystem
                )(
                    Start(Uri.unsafeFromString(cfg.fetchUrl.url))
                ).crawl
            server = cfg.serverConfig
            _ <- BlazeServerBuilder[F]
                  .bindHttp(server.port, server.host)
                  .withHttpApp(api.httpApp)
                  .serve
          } yield ()
    }

  def nativeHttp[F[_]: Concurrent](cfg: Config): Server[F] = ???
}
