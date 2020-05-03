package programs

import algebras.{Downloader, FileSystem}
import algebras.Extractor.{CssUri, HtmlResource, ImgUri, JsUri, LinkUri}
import cats.syntax.apply._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect.{Concurrent, Sync, Timer}
import cats.effect.concurrent.Ref
import domain.page.HtmlContent
import fs2._
import org.http4s.Uri
import org.slf4j.Logger
import algebras.Extractor.syntax._
import utils.XmlTraversable

import scala.concurrent.duration._

trait Crawler[F[_]] {
  def crawl: Stream[F, Unit]
}

object Crawler {
  case class Start(uri: Uri)
  def apply[F[_]: Concurrent: Timer](fetcher: Downloader[F], fs: FileSystem[F])(
        startUri: Start
  )(implicit L: Logger): Crawler[F] =
    new Crawler[F] {
      type Traversed[G[_]] = Ref[G, List[LinkUri]]         // traversed and modified resources
      type Next[G[_]]      = Ref[G, Option[List[LinkUri]]] // nodes, we are going to fetch

      def crawl: Stream[F, Unit] =
        Stream
          .eval {
            (Ref.of[F, List[LinkUri]](List.empty)
              -> Ref.of[F, Option[List[LinkUri]]](Some(List.empty))).tupled
          }
          .flatMap {
            case (traversed, next) =>
              /**
                * We start with 'start URL' (index.html) to collect enough info
                * for fetching resources and other pages in parallel
                */
              startPipeline(traversed, next, LinkUri(startUri.uri))
          }

      def fetch(link: HtmlResource)(continue: HtmlContent => Stream[F, Unit]): Stream[F, Unit] =
        Stream
          .eval(fetcher.fetchPage(link.uri))
          .flatMap(
              _.fold(
                Stream
                .raiseError[F](new RuntimeException("Can't fetch page")): Stream[F, Unit]
            )(continue)
          )

      def startPipeline(traversed: Traversed[F], next: Next[F], currentLink: HtmlResource): Stream[F, Unit] =
        fetch(currentLink) { content =>
          val pipeline =
            for {
              _ <- currentLink match {
                    case link: LinkUri => traversed.update(link :: _)
                    case _             => Sync[F].pure(())
                  }
              resources <- extract(content)
              nextLinks = resources.collect { case link: LinkUri => link }
              _              <- next.update(maybe => Some(maybe.getOrElse(List.empty) ++ nextLinks))
              updatedContent <- XmlTraversable.modify[F](content)(resources)
            } yield updatedContent -> resources

          Stream.eval(pipeline).flatMap {
            case (content, resources) =>
              fs.writeFile(currentLink.toPath, content) ++
                Stream
                  .emits(resources)
                  .covary[F]
                  .collect {
                    case r: HtmlResource if r.isInstanceOf[JsUri] || r.isInstanceOf[CssUri] || r.isInstanceOf[ImgUri] =>
                      r
                  }
                  .map(r => fetch(r)(fs.writeFile(r.toPath, _)))
                  .parJoinUnbounded
          }
        }

      val extract: HtmlContent => F[List[HtmlResource]] =
        html =>
          for {
            styles  <- html.extract[F, CssUri]
            scripts <- html.extract[F, JsUri]
            links   <- html.extract[F, LinkUri]
            imgs    <- html.extract[F, ImgUri]
          } yield (styles ++ scripts ++ links ++ imgs).distinct
            .collect {
              case resource: HtmlResource if resource.uri.host.isDefined => resource
            }

      val concurrentCheck: Next[F] => Stream[F, Unit] =
        next =>
          Stream.awakeEvery[F](500.millis) >>
            Stream
              .eval(next.get)
              .unNone
              .evalMap {
                case Nil          => next.update(_ => None)
                case _ @ ::(_, _) => Sync[F].pure(())
              }
    }
}
