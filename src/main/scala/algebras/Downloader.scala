package algebras

import cats.ApplicativeError
import cats.effect.{Concurrent, ConcurrentEffect, Resource, Sync, Timer}
import domain.page.HtmlContent
import org.http4s.{Request, Uri}
import org.http4s.client.Client
import fs2._
import org.http4s.client.asynchttpclient.AsyncHttpClient
import scala.concurrent.duration._
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.http4s.util.threads.threadFactory
import org.slf4j.Logger

trait Downloader[F[_]] {
  def fetchPage(url: Uri): F[Option[HtmlContent]]
}

/**
  * Represents http client, that can fetch specified URL
  * and all it's content using AsyncHttpClient backend wrapped over
  * pure [[org.http4s.client.Client]]
  */
object AsyncDownloader {
  def make[F[_]: Concurrent: Timer: ConcurrentEffect](
        implicit E: ApplicativeError[F, Throwable]
      , L: Logger
  ): Resource[F, Downloader[F]] =
    AsyncHttpClient
      .resource[F](
          new DefaultAsyncHttpClientConfig.Builder()
          .setMaxConnectionsPerHost(100)
          .setMaxConnections(200)
          .setRequestTimeout(2.seconds.toMillis.toInt)
          .setThreadFactory(threadFactory(name = { i =>
            s"http4s-async-http-client-worker-$i"
          }))
          .build()
      )
      .map(new AsyncDownloader[F](_))
}

final class AsyncDownloader[F[_]: Concurrent: Timer] private (asyncHttp: Client[F])(
      implicit E: ApplicativeError[F, Throwable]
    , L: Logger
) extends Downloader[F] {
  def fetchPage(uri: Uri): F[Option[HtmlContent]] =
    asyncHttp
      .run(Request(uri = uri))
      .evalMap { resp =>
        resp.body
          .through(text.utf8Decode)
          .compile
          .toList
      }
      .map(list => Some(HtmlContent(list.mkString)))
      .use(Sync[F].pure(_))
}
