package utils

import java.io.File

import domain.page.HtmlContent
import scalatags.Text.all._
import scalatags.Text.short.*

object HtmlConstructor {
  def render(hrefs: List[File]): HtmlContent = {
    val as =
      hrefs.map { file =>
        val name = file.getName
        div(a(*.href := s"./$name")(s"$name"))
      }

    HtmlContent(
        tag("html")(
          tag("head")(
            p("crawler has finished his work. Following pages has been downloaded")
        )
        , tag("body")(
            as
        )
      ).render
    )
  }
}
