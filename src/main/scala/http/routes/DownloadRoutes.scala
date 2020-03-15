package http.routes

import cats.effect.Sync
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class DownloadRoutes[F[_]: Sync] extends Http4sDsl[F] {
  private[routes] val prefixPath = "/download"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root => ???
  }

  val routes: HttpRoutes[F] = Router(
      prefixPath -> httpRoutes
  )
}
