package algebras

import java.io.File

import algebras.Extractor.HtmlResource
import cats.effect.Sync
import cats.syntax.functor._
import domain.page.HtmlContent
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import org.http4s.Uri
import cats.syntax.traverse._
import cats.instances.list._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.HtmlExtractor
import org.slf4j.LoggerFactory
import CrawlerFileSystem.defaultDir

trait Extractor[F[_], T <: HtmlResource] {
  def extract: HtmlContent => F[List[T]]
}

object Extractor {
  type Query = String
  final case class ExtractFailure(description: String) extends Throwable
  private[Extractor] val L = LoggerFactory.getLogger(getClass)

  def apply[F[_], T <: HtmlResource](implicit E: Extractor[F, T]): Extractor[F, T] = E

  sealed trait HtmlResource extends Product with Serializable {
    val uri: Uri

    val query: Query
    val attributeKey: String

    protected def extractLast: String = uri.path.split("/").lastOption.getOrElse("")

    def toIndex: File = new File(s"./${CrawlerFileSystem.defaultDir}/index.html")
    def toPathWithoutDir: File
    def toPath: File
  }
  case class JsUri(uri: Uri) extends HtmlResource {
    val attributeKey: String   = "src"
    val query: Query           = s"${JsUri.js}[$attributeKey*=${uri.toString}]"
    def toPath: File           = new File(s"$defaultDir/js/$extractLast")
    def toPathWithoutDir: File = new File(s"./$extractLast")
  }
  object JsUri {
    private val js = "script"
    implicit def jsExtractor[F[_]: Sync]: Extractor[F, JsUri] = new Extractor[F, JsUri] {
      def extract: HtmlContent => F[List[JsUri]] =
        extractCommon[F, JsUri](attrs("src")(s"$js"))(
            JsUri.apply
        )
    }
  }
  case class CssUri(uri: Uri) extends HtmlResource {
    val attributeKey: String   = "href"
    val query: Query           = s"link[rel=${CssUri.css} $attributeKey*=${uri.toString}]"
    def toPath: File           = new File(s"$defaultDir/css/$extractLast")
    def toPathWithoutDir: File = new File(s"./$extractLast")
  }
  object CssUri {
    private val css = "stylesheet"
    implicit def cssExtractor[F[_]: Sync]: Extractor[F, CssUri] = new Extractor[F, CssUri] {
      override def extract: HtmlContent => F[List[CssUri]] =
        extractCommon[F, CssUri](attrs("href")(s"link[rel=$css]"))(
            CssUri.apply
        )
    }
  }
  case class LinkUri(uri: Uri) extends HtmlResource {
    val attributeKey: String   = "href"
    val query: Query           = s"${LinkUri.link}[$attributeKey*=${uri.toString}]"
    def toPath: File           = new File(s"$defaultDir/${uri.host.get}.html")
    def toPathWithoutDir: File = new File(s"./${uri.host.get}.html")
  }
  object LinkUri {
    private val link = "a"
    implicit def cssExtractor[F[_]: Sync]: Extractor[F, LinkUri] = new Extractor[F, LinkUri] {
      override def extract: HtmlContent => F[List[LinkUri]] =
        extractCommon[F, LinkUri](attrs("href")(s"$link"))(
            LinkUri.apply
        )
    }
  }
  case class ImgUri(uri: Uri) extends HtmlResource {
    val attributeKey: String   = "src"
    val query: Query           = s"${ImgUri.image}[$attributeKey*=${uri.toString}]"
    def toPath: File           = new File(s"$defaultDir/img/$extractLast")
    def toPathWithoutDir: File = new File(s"./$extractLast")
  }
  object ImgUri {
    private val image = "img"
    implicit def cssExtractor[F[_]: Sync]: Extractor[F, ImgUri] = new Extractor[F, ImgUri] {
      override def extract: HtmlContent => F[List[ImgUri]] =
        extractCommon[F, ImgUri](attrs("src")(s"$image"))(
            ImgUri.apply
        )
    }
  }

  type HtmlE = HtmlExtractor[Element, Iterable[String]]
  private def extractCommon[F[_]: Sync, T <: HtmlResource](ext: HtmlE)(apply: Uri => T): HtmlContent => F[List[T]] =
    (html: HtmlContent) =>
      (JsoupBrowser().parseString(html.value) >?> ext)
        .fold(Sync[F].delay(L.info("nothing to extract")).as(List.empty[T])) {
          _.toList
            .flatTraverse(
                l =>
                Uri
                  .fromString(l)
                  .fold(
                      _ =>
                      Sync[F]
                        .delay(L.info(s"can't parse: $l"))
                        .as(List.empty[T])
                    , x => Sync[F].pure(List(apply(x)))
                  )
            )
        }

  object syntax {
    implicit class PageSyntax(page: HtmlContent) {
      def extract[F[_], T <: HtmlResource](implicit E: Extractor[F, T]): F[List[T]] =
        Extractor[F, T].extract(page)
    }
  }
}
