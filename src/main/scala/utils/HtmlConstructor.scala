package utils

import java.io.File

import scalatags.Text.all._
import scalatags.Text.short.*

object HtmlConstructor {
  def render(hrefs: List[File]): String = {
    val as =
      hrefs.map { file =>
        val path = file.toPath.toString
        div(a(*.href := path)(s"$path"))
      }

    tag("html")(
        tag("head")(
          p("crawler has finished his work. Following pages has been downloaded")
      )
      , tag("body")(
          as
      )
    ).render
  }
}
