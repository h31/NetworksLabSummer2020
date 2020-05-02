package programs

import algebras.{Downloader, FileSystem}
import algebras.CrawlerCache.Index
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect.Sync
import cats.effect.concurrent.Ref
import fs2._
import org.http4s.Uri
import org.slf4j.Logger

import scala.reflect.io.File

trait Crawler[F[_]] {
  def crawl: Stream[F, Unit]
}

object Crawler {
  case class Start(uri: Uri)
  def apply[F[_]: Sync](fetcher: Downloader[F], cache: Index[F], fs: FileSystem[F])(startUri: Start)(
        implicit L: Logger
  ): Crawler[F] =
    new Crawler[F] {
      def crawl: Stream[F, Unit] =
        Stream.eval(Ref.of[F, List[File]](List.empty)).flatMap { traversedNodes =>
          ???
        }

      /**
        * We start with start URL (index.html) to collect enough info
        * to start fetching resources and other pages in parallel
        */
      val startPipeline: Ref[F, List[File]] => Stream[F, Unit] =
        traversed =>
          Stream
            .eval(
                fetcher
                .fetchPage(startUri.uri)
            )
            .flatMap(_.fold(Stream.raiseError[F](new RuntimeException("Can't fetch page, probably wrong URL"))) {
              content =>
                ???
            })

    }
}
