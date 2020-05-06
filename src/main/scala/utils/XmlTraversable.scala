package utils

import algebras.Extractor.HtmlResource
import cats.effect.Sync
import domain.page.HtmlContent
import org.jsoup.Jsoup

object XmlTraversable {
  def modify[F[_]: Sync](html: HtmlContent)(modify: List[HtmlResource]): F[HtmlContent] =
    Sync[F]
      .delay { // local mutability
        val doc = Jsoup.parse(html.value)
        modify.foreach(
            res => doc.select(res.query).attr(res.attributeKey, s"${res.toPathWithoutDir}")
        )
        HtmlContent(doc.html())
      }
}
