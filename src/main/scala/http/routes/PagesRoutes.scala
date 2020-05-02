package http.routes

import cats.effect.Sync
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class PagesRoutes[F[_]: Sync]() extends Http4sDsl[F] {
  private[routes] val prefixPath = "/pages"

  /**
    * construct a page with links to downloaded
    * pages, each one has relative paths to the server folder
    */
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => ???
  }

  val routes: HttpRoutes[F] = Router(
      prefixPath -> httpRoutes
  )
}
