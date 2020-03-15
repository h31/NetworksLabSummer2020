package algebras

import java.net.URL

import cats.effect.{ConcurrentEffect, Resource}
import domain.page.Page
import org.http4s.client.Client
import org.http4s.client.asynchttpclient.AsyncHttpClient

trait Downloader[F[_]] {
  def fetch(url: URL): F[Option[Page]]
}

/**
  * Represents http client, that can fetch specified URL
  * and all it's content using AsyncHttpClient backend wrapped over
  * pure [[org.http4s.client.Client]]
  */
object AsyncDownloader {
  def make[F[_]]()(implicit C: ConcurrentEffect[F]): F[Resource[F, Downloader[F]]] =
    C.delay(
        AsyncHttpClient.resource[F]().map(new AsyncDownloader[F](_))
    )
  // TODO maybe inline
}

final class AsyncDownloader[F[_]] private (asyncHttp: Client[F]) extends Downloader[F] {
  def fetch(url: URL): F[Option[Page]] = ???
}

/**
  * My implementation of page-downloader of specified URL
  * It is unsafe, because should of course contain a lot of bugs
  */
object NativeDownloader {
  def unsafe[F[_]]()(implicit C: ConcurrentEffect[F]): F[Resource[F, Downloader[F]]] = ???
}
