package programs

import java.io.File

import algebras.{Downloader, FileSystem}
import algebras.Extractor.{CssUri, ExtractType, ImgUri, JsUri, LinkUri}
import cats.syntax.apply._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import domain.page.HtmlContent
import fs2._
import org.http4s.Uri
import org.slf4j.Logger
import utils.UriUtils.UriOps
import algebras.Extractor.syntax._

import scala.concurrent.duration._

trait Crawler[F[_]] {
  def crawl: Stream[F, Unit]
}

object Crawler {
  case class Start(uri: Uri)
  def apply[F[_]: Sync: Concurrent: Timer](fetcher: Downloader[F], fs: FileSystem[F])(
        startUri: Start
  )(implicit L: Logger): Crawler[F] =
    new Crawler[F] {
      type Traversed[G[_]] = Ref[G, List[File]]            // traversed and modified resources
      type Next[G[_]]      = Ref[G, Option[List[LinkUri]]] // nodes, we are going to fetch

      def crawl: Stream[F, Unit] =
        Stream
          .eval {
            (Ref.of[F, List[File]](List.empty)
              -> Ref.of[F, Option[List[LinkUri]]](Some(List.empty))).tupled
          }
          .flatMap {
            case (traversed, next) =>
              /**
                * We start with 'start URL' (index.html) to collect enough info
                * for fetching resources and other pages in parallel
                */
              (startPipeline(traversed)(LinkUri(startUri.uri)) ++
                pipelines(traversed, next))
                .concurrently(
                    concurrentCheck(next)
                )
          }

      val startPipeline: Traversed[F] => LinkUri => Stream[F, Unit] =
        traversed =>
          link =>
            Stream
              .eval(
                  fetcher
                  .fetchPage(link.uri)
              )
              .flatMap(
                  _.fold(Stream.raiseError[F](new RuntimeException("Can't fetch start page, probably wrong URL"))) {
                  content =>
                    // update traversed links, extract link andThen update index
                    for {
                      _ <- traversed.update(link.uri.toIndex :: _)
                      _ <- extract(content)
                    } yield ()

                    ???
                }
              )

      def pipelines(traversed: Traversed[F], next: Next[F]): Stream[F, Unit] =
        Stream
          .eval(next.get)
          .unNoneTerminate
          .flatMap(Stream.emits(_).covary[F].map(startPipeline(traversed)(_)))
          .parJoin(5) // point of parallelism
          .repeat
          .onComplete(Stream.eval(Sync[F].delay(L.info("Crawler finished"))))

      val extract: HtmlContent => F[Set[ExtractType]] = // TODO fix
        html =>
          (for {
            styles  <- html.extract[CssUri]
            scripts <- html.extract[JsUri]
            links   <- html.extract[LinkUri]
            imgs    <- html.extract[ImgUri]
          } yield (styles ++ scripts ++ links ++ imgs).toSet)
            .fold(fa => Sync[F].delay(L.info(s"extraction error: $fa")).as(Set.empty), Sync[F].pure)

      val concurrentCheck: Next[F] => Stream[F, Unit] =
        next =>
          Stream.awakeEvery[F](500.millis) ++
            Stream
              .eval(next.get)
              .unNone
              .evalMap {
                case Nil             => next.update(_ => None)
                case list @:: (_, _) => Sync[F].pure(list)
              }
    }
}
