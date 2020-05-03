import algebras.{AsyncDownloader, CrawlerFileSystem}
import cats.effect.{Blocker, Concurrent, ConcurrentEffect, ContextShift, Timer}
import config.Config
import fs2._
import org.http4s.Uri
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.Logger
import programs.Crawler
import programs.Crawler.Start
import cats.syntax.either._
import cats.syntax.functor._
import http.HttpApi

import scala.concurrent.duration._

trait Server[F[_]] {
  def start: Stream[F, Unit]
}

object Server {
  def blazeHttp[F[_]: Timer: ConcurrentEffect](
        cfg: Config
  )(implicit C: ContextShift[F], B: Blocker, L: Logger): Server[F] =
    new Server[F] {
      def start: Stream[F, Unit] = {
        val server = cfg.serverConfig

        val env = for {
          downloader <- Stream.resource(AsyncDownloader.make[F])
          fileSystem <- Stream.eval(CrawlerFileSystem[F])
          api = HttpApi.V1.api
        } yield (downloader, fileSystem, api.httpApp)

        env.flatMap {
          case (fetcher, fs, api) =>
            val crawlerStream =
              Crawler[F](fetcher, fs)(
                  Start(Uri.unsafeFromString(cfg.fetchUrl.url))
              ).crawl
                .interruptWhen[F](
                    Timer[F].sleep(cfg.fetchTime.time.seconds).as(().asRight[Throwable])
                ) // use crawler only once during cfg.time

            crawlerStream.handleErrorWith(
                err => Stream.eval(ConcurrentEffect[F].delay(L.info(err.getMessage)))
            ) ++
              BlazeServerBuilder[F] // use crawler, after that start http server
                .bindHttp(server.port, server.host)
                .withHttpApp(api)
                .serve
                .void
        }
      }
    }

  def nativeHttp[F[_]: Concurrent](cfg: Config): Server[F] = ???
}
