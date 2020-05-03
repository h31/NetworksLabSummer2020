import java.io.File

import algebras.Extractor.{ImgUri, LinkUri}
import cats.effect.IO
import domain.page.HtmlContent
import org.http4s.Uri
import org.scalatest.{FlatSpec, Matchers}
import utils.XmlTraversable

class XmlModifierSpec extends FlatSpec with Matchers {

  "XMl-modifier" should "change path's to local" in {
    val dom =
      HtmlContent(<html>
        <head></head>
        <body>
        <div class="bmenu">
        <a
        class="bmenu__conversion"
        href="https://habr.com/sandbox/start/"
        onclick="if (typeof ga === 'function') { ga('send', 'event', 'habr_top_panel', 'become_an_author'); }"
        >Как стать автором</a>
        <a class="bmenu__theme"
           href="https://marathon.habr.com/?utm_source=habr_header"
           target="_blank"
           onclick="if (typeof ga === 'function') { ga('send', 'event', 'habr_top_panel', 'lightning', 'marathon'); }">
          <img alt="" width="20" style="position:relative; vertical-align: middle; top: -2px; margin-right: 4px;" 
               src="https://habrastorage.org/getpro/tmtm/pictures/191/7b2/ec0/1917b2ec03aa04d759e41c950732e07e.png" 
          />Марафон удалёнки</a>
      </div>
      </body>
        </html>.toString)

    val modified =
      XmlTraversable.modify[IO](dom)(
          List(
            LinkUri(Uri.unsafeFromString("https://habr.com/sandbox/start/"))
          , ImgUri(
              Uri.unsafeFromString(
                "https://habrastorage.org/getpro/tmtm/pictures/191/7b2/ec0/1917b2ec03aa04d759e41c950732e07e.png"
            )
          )
        )
      )

    val expected =
      HtmlContent(<html>
        <head></head>
        <body>
          <div class="bmenu">
            <a
            class="bmenu__conversion"
            href="hoho"
            onclick="if (typeof ga === 'function') { ga('send', 'event', 'habr_top_panel', 'become_an_author'); }"
            >Как стать автором</a>
            <a class="bmenu__theme" href="https://marathon.habr.com/?utm_source=habr_header" target="_blank" onclick="if (typeof ga === 'function') { ga('send', 'event', 'habr_top_panel', 'lightning', 'marathon'); }"><img alt="" width="20" style="position:relative; vertical-align: middle; top: -2px; margin-right: 4px;" src="haha" />Марафон удалёнки</a>
          </div>
        </body>
      </html>.toString)

    // to use, please do unsafeRunSync()
    modified.map(_ shouldEqual expected)
  }
}
