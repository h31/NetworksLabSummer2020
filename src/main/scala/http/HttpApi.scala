package http

import cats.effect.Concurrent
import cats.implicits._
import org.http4s.implicits._
import http.routes.{DownloadRoutes, PagesRoutes}
import org.http4s.server.Router
import org.http4s.server.middleware.{RequestLogger, ResponseLogger}
import org.http4s.{HttpApp, HttpRoutes}

object HttpApi {
  def apply[F[_]](implicit C: Concurrent[F]): F[HttpApi[F]] =
    C.delay(new HttpApi)
}

final class HttpApi[F[_]: Concurrent] private {
  private val pagesRoutes    = new PagesRoutes[F].routes
  private val downloadRoutes = new DownloadRoutes[F].routes

  private val baseRoutes: HttpRoutes[F] =
    pagesRoutes <+> downloadRoutes

  private val routes: HttpRoutes[F] = Router(
      version.v1 -> baseRoutes
  )

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(logHeaders = true, logBody = true)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(logHeaders = true, logBody = true)(http)
    }
  }

  val httpApp: HttpApp[F] = routes.orNotFound

  /**
    * defines base routes as httpApp, but have also
    * loggers for each req/resp at MiddleWare
    */
  val httpAppWithLg: HttpApp[F] = loggers(routes.orNotFound)
}
