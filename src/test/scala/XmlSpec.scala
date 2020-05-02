import algebras.Extractor
import algebras.Extractor.{CssUri, ImgUri, JsUri, LinkUri}
import domain.page.HtmlContent
import org.scalatest.{FlatSpec, Matchers}
import Extractor.syntax._
import cats.syntax.either._
import org.http4s.Uri

class XmlSpec extends FlatSpec with Matchers {
  "Css extractor" should "extract css links from html DOM" in {
    val simpleCssLink =
      HtmlContent(
          <head>
          <link rel ="stylesheet" href ="https://microsoft.com"/>
        </head>.toString
      )

    simpleCssLink
      .extract[CssUri] shouldEqual List(CssUri(Uri.unsafeFromString("https://microsoft.com"))).asRight
  }

  "Js extractor" should "extract js links from html DOM" in {
    val simpleJsLink =
      HtmlContent(
          <script src="https://dr.habracdn.net/habrcom/javascripts/1588261330/libs/jquery-1.8.3.min.js">
        </script>.toString
      )

    simpleJsLink.extract[JsUri] shouldEqual List(
        JsUri(Uri.unsafeFromString("https://dr.habracdn.net/habrcom/javascripts/1588261330/libs/jquery-1.8.3.min.js"))
    ).asRight
  }

  "Link extractor" should "extract links to other pages from html DOM" in {
    val link = HtmlContent(
        <a class="service" 
           href="https://career.habr.com?utm_source=habrutm_medium=habr_top_panel"
        />.toString
    )
    val otherLink =
      HtmlContent(
          <a href="https://u.tmtm.ru/pt_top" 
           target="_blank" 
           style="color: #F46F6F" 
           rel=" noopener">The Standoff: шоу</a>.toString
      )

    link.extract[LinkUri] shouldEqual List(
        LinkUri(Uri.unsafeFromString("https://career.habr.com?utm_source=habrutm_medium=habr_top_panel"))
    ).asRight

    otherLink.extract[LinkUri] shouldEqual List(
        LinkUri(Uri.unsafeFromString("https://u.tmtm.ru/pt_top"))
    ).asRight
  }

  "Img extractor" should "extract image links from html DOM" in {
    val link = HtmlContent(
        <img src="https://andr83.io/wp-content/uploads/2018/04/magic_hat.png" alt="image"/>.toString
    )

    link.extract[ImgUri] shouldEqual List(
        ImgUri(Uri.unsafeFromString("https://andr83.io/wp-content/uploads/2018/04/magic_hat.png"))
    ).asRight
  }

  "Test for all type" should "extract right" in {
    val dom =
      HtmlContent(<div class="bmenu">
        <a
        class="bmenu__conversion"
        href="https://habr.com/sandbox/start/"
        onclick="if (typeof ga === 'function') { ga('send', 'event', 'habr_top_panel', 'become_an_author'); }"
        >Как стать автором</a>

        <a
        class="bmenu__theme"
        href="https://marathon.habr.com/?utm_source=habr_header"
        target="_blank"
        onclick="if (typeof ga === 'function') { ga('send', 'event', 'habr_top_panel', 'lightning', 'marathon'); }"
        >
          <img alt="" width="20" style="position:relative; vertical-align: middle; top: -2px; margin-right: 4px;" src="https://habrastorage.org/getpro/tmtm/pictures/191/7b2/ec0/1917b2ec03aa04d759e41c950732e07e.png" />
          Марафон удалёнки
        </a>
      </div>.toString)

    val expected =
      List(
          "https://habrastorage.org/getpro/tmtm/pictures/191/7b2/ec0/1917b2ec03aa04d759e41c950732e07e.png"
        , "https://habr.com/sandbox/start/"
        , "https://marathon.habr.com/?utm_source=habr_header"
      ).map(Uri.unsafeFromString)

    val result = (for {
      imgs <- dom.extract[ImgUri]
      as   <- dom.extract[LinkUri]
    } yield imgs ++ as).getOrElse(List.empty).map(_.uri)

    result shouldEqual expected
  }
}
