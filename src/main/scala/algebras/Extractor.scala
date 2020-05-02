package algebras

import algebras.Extractor.ExtractTypes
import domain.page.HtmlContent
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.http4s.Uri
import cats.syntax.traverse._
import cats.syntax.either._
import cats.instances.either._
import cats.instances.list._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.HtmlExtractor

trait Extractor[T <: ExtractTypes] {
  def extract: HtmlContent => Extractor.Result[T]
}

object Extractor {
  type Result[T] = Either[ExtractFailure, List[T]]
  final case class ExtractFailure(description: String) extends Throwable

  def apply[T <: ExtractTypes](implicit E: Extractor[T]): Extractor[T] = E

  sealed trait ExtractTypes extends Product with Serializable {
    val uri: Uri
  }
  case class JsUri(uri: Uri) extends ExtractTypes
  object JsUri {
    private val js = "script"
    implicit val jsExtractor: Extractor[JsUri] = new Extractor[JsUri] {
      def extract: HtmlContent => Result[JsUri] =
        extractCommon[JsUri](attrs("src")(s"$js"))("Can't extract data for js")(
            JsUri.apply
        )
    }
  }
  case class CssUri(uri: Uri) extends ExtractTypes
  object CssUri {
    private val css = "stylesheet"
    implicit val cssExtractor: Extractor[CssUri] = new Extractor[CssUri] {
      override def extract: HtmlContent => Result[CssUri] =
        extractCommon[CssUri](attrs("href")(s"link[rel=$css]"))("Can't extract data for css")(
            CssUri.apply
        )
    }
  }
  case class LinkUri(uri: Uri) extends ExtractTypes
  object LinkUri {
    private val link = "a"
    implicit val cssExtractor: Extractor[LinkUri] = new Extractor[LinkUri] {
      override def extract: HtmlContent => Result[LinkUri] =
        extractCommon[LinkUri](attrs("href")(s"$link"))("Can't extract data for `a` element")(
            LinkUri.apply
        )
    }
  }
  case class ImgUri(uri: Uri) extends ExtractTypes
  object ImgUri {
    private val image = "img"
    implicit val cssExtractor: Extractor[ImgUri] = new Extractor[ImgUri] {
      override def extract: HtmlContent => Result[ImgUri] =
        extractCommon[ImgUri](attrs("src")(s"$image"))("Can't extract data for img")(
            ImgUri.apply
        )
    }
  }

  type HtmlE = HtmlExtractor[Element, Iterable[String]]
  private def extractCommon[T <: ExtractTypes](
        ext: HtmlE
  )(ifFailure: String)(apply: Uri => T): HtmlContent => Extractor.Result[T] =
    (html: HtmlContent) =>
      (JsoupBrowser().parseString(html.value) >?> ext)
        .fold(ExtractFailure(ifFailure).asLeft[List[T]]) {
          _.toList
            .traverse(Uri.fromString(_).map(apply))
            .left
            .map(flr => ExtractFailure(flr.details))
        }

  object syntax {
    implicit class PageSyntax(page: HtmlContent) {
      def extract[T <: ExtractTypes](implicit E: Extractor[T]): Extractor.Result[T] = Extractor[T].extract(page)
    }
  }
}
