package algebras

import cats.effect.{ConcurrentEffect, Resource, Sync}
import domain.page.{Content, Page}
import org.http4s.{Request, Uri}
import org.http4s.client.Client
import org.http4s.client.asynchttpclient.AsyncHttpClient
import fs2._

trait Downloader[F[_]] {
  def fetchPage(url: Uri): F[Option[Page]]
}

/**
  * Represents http client, that can fetch specified URL
  * and all it's content using AsyncHttpClient backend wrapped over
  * pure [[org.http4s.client.Client]]
  */
object AsyncDownloader {
  def make[F[_]](implicit C: ConcurrentEffect[F]): F[Resource[F, Downloader[F]]] =
    C.delay(
        AsyncHttpClient.resource[F]().map(new AsyncDownloader[F](_))
    )
}

final class AsyncDownloader[F[_]: Sync] private (asyncHttp: Client[F]) extends Downloader[F] {
  def fetchPage(uri: Uri): F[Option[Page]] =
    asyncHttp
      .run(Request(uri = uri))
      .evalMap { resp =>
        resp.body
          .through(text.utf8Decode)
          .compile
          .toList
      }
      .map(list => Some(Page(Content(list.mkString("\n")))))
      .use(Sync[F].pure(_))
}

/**
  * My implementation of page-downloader of specified URL
  * It is unsafe, because should of course contain a lot of bugs
  */
object NativeDownloader {
  def unsafe[F[_]]()(implicit C: ConcurrentEffect[F]): F[Resource[F, Downloader[F]]] = ???
}
