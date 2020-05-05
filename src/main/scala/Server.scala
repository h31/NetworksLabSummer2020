import java.io.File
import java.net.InetSocketAddress

import algebras.{AsyncDownloader, CrawlerFileSystem, FileSystem}
import cats.effect.{Blocker, Concurrent, ConcurrentEffect, ContextShift, Timer}
import config.Config
import io.circe.syntax._
import io.circe.generic.auto._
import fs2._
import org.http4s.Uri
import org.http4s.server.blaze.BlazeServerBuilder
import org.slf4j.Logger
import programs.Crawler
import programs.Crawler.Start
import cats.syntax.either._
import cats.syntax.functor._
import fs2.io.tcp.SocketGroup
import http.HttpApi
import native.{HttpSocket, NaiveMessageAnalyzer}
import utils.HtmlConstructor

import scala.concurrent.duration._

trait Server[F[_]] {
  def start: Stream[F, Unit]
}

object Server {

  /**
    * 1) Starts crawler stream for limited amount of time.
    *
    * 2) After he completed his work, start render stream, that will render
    * page with fetched links and save it to filesystem default folder.
    *
    * 3) Start server implementation
    */
  private def withServer[F[_]: ConcurrentEffect: Concurrent: Timer](
        cfg: Config
  )(
        serverImpl: FileSystem[F] => Stream[F, Unit]
  )(implicit L: Logger, C: ContextShift[F], B: Blocker): Stream[F, Unit] = {
    val env = for {
      downloader <- Stream.resource(AsyncDownloader.make[F])
      fileSystem <- Stream.eval(CrawlerFileSystem[F])
    } yield (downloader, fileSystem)

    env.flatMap {
      case (fetcher, fs) =>
        val crawlerStream =
          Crawler[F](fetcher, fs)(
              Start(Uri.unsafeFromString(cfg.fetchUrl.url))
          ).crawl
            .interruptWhen[F](
                Timer[F].sleep(cfg.fetchTime.time.seconds).as(().asRight[Throwable])
            ) // use crawler only once during cfg.time

        val renderStream =
          Stream
            .eval(fs.scan(_.toPath.toString.contains(".html")))
            .map(HtmlConstructor.render)
            .flatMap(
                fs.writeFile(
                  new File(s"./${CrawlerFileSystem.defaultDir}/index.html")
                , _
              )
            )

        crawlerStream.handleErrorWith(
            err => Stream.eval(ConcurrentEffect[F].delay(L.info(err.getMessage)))
        ) ++ renderStream ++ serverImpl(fs)
    }
  }

  def blazeHttp[F[_]: Timer: ConcurrentEffect](
        cfg: Config
  )(implicit C: ContextShift[F], B: Blocker, L: Logger): Server[F] =
    new Server[F] {
      def start: Stream[F, Unit] = {
        val server = cfg.serverConfig
        val serverImpl = BlazeServerBuilder[F]
          .bindHttp(server.port, server.host)
          .withHttpApp(HttpApi.V1.api.httpApp)
          .serve
          .void

        withServer[F](cfg)(_ => serverImpl)
      }
    }

  /**
    * Naive native very bad implementation of a server based on sockets
    */
  def nativeHttp[F[_]](cfg: Config)(
        implicit CS: ContextShift[F]
      , C: Concurrent[F]
      , CE: ConcurrentEffect[F]
      , T: Timer[F]
      , B: Blocker
      , L: Logger
  ): Server[F] =
    new Server[F] {
      def start: Stream[F, Unit] = {
        val pipeline: FileSystem[F] => HttpSocket[F] => Stream[F, Unit] =
          fs =>
            connection => {
              for {
                msg <- connection.read
                _   <- Stream.eval(C.delay(println(s"following msg has been received: $msg")))
                out <- Stream.eval(NaiveMessageAnalyzer.analyze[F](fs)(msg))
                _   <- Stream.eval(connection.write(out))
              } yield ()
            }

        withServer[F](cfg) { fs =>
          Stream.eval(C.delay(L.info(s"Server started with config:\n ${cfg.asJson}"))) >>
            Stream
              .resource(SocketGroup[F](B))
              .flatMap { socketGroup =>
                socketGroup.server[F](
                    address = new InetSocketAddress(cfg.serverConfig.host, cfg.serverConfig.port)
                )
              }
              .map { connectedClient =>
                Stream.resource(connectedClient).flatMap { socket =>
                  Stream.eval(HttpSocket[F](socket)).flatMap(pipeline(fs))
                }
              }
              .parJoinUnbounded
        }
      }
    }
}
