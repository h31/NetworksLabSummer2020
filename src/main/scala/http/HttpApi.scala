package http

import algebras.CrawlerFileSystem
import cats.effect.{Blocker, ConcurrentEffect, ContextShift}
import org.http4s.implicits._
import org.http4s.server.middleware.{RequestLogger, ResponseLogger}
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.server.staticcontent._

object HttpApi {
  sealed trait HttpApi[F[_]] { val httpApp: HttpApp[F] }

  case object V1 {
    def api[F[_]: ConcurrentEffect](
          implicit B: Blocker
        , C: ContextShift[F]
    ): HttpApi[F] =
      new V1[F]
  }

  final class V1[F[_]: ConcurrentEffect] private (
        implicit
      B: Blocker
      , C: ContextShift[F]
  ) extends HttpApi[F] {

    private val staticRoutes =
      fileService[F](
          FileService.Config(
            systemPath = s"./${CrawlerFileSystem.defaultDir}"
          , executionContext = B.blockingContext
        )
      )

    private val routes: HttpRoutes[F] = staticRoutes

    private val loggers: HttpApp[F] => HttpApp[F] = {
      { http: HttpApp[F] =>
        RequestLogger.httpApp(logHeaders = true, logBody = true)(http)
      } andThen { http: HttpApp[F] =>
        ResponseLogger.httpApp(logHeaders = true, logBody = true)(http)
      }
    }

    /**
      * defines base routes as httpApp, but have also
      * loggers for each req/resp at Middleware
      */
    val httpApp: HttpApp[F] = loggers(routes.orNotFound)
  }
}
