package utils

import java.io.File

import algebras.Extractor.ExtractType
import cats.effect.Sync
import domain.page.HtmlContent
import org.jsoup.Jsoup

object XmlTraversable {
  def modify[F[_]: Sync](html: HtmlContent)(fromTo: Map[ExtractType, File]): F[HtmlContent] =
    Sync[F]
      .delay { // local mutability
        val doc = Jsoup.parse(html.value)
        fromTo.foreach {
          case (from, to) =>
            doc.select(from.query).attr(from.attributeKey, s"$to")
        }
        HtmlContent(doc.html())
      }
}
