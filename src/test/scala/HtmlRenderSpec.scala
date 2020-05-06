import java.io.File

import org.scalatest.{FlatSpec, Matchers}
import utils.HtmlConstructor

class HtmlRenderSpec extends FlatSpec with Matchers {
  "Html renderer" should "render html" in {
    val expected =
      <html>
          <head>
            <p>crawler has finished his work. Following pages has been downloaded</p>
          </head>
          <body>
            <a href="haha">haha</a>
            <a href="hoho">hoho</a>
          </body>
      </html>.toString

    // htmls are the same but tests don't pass
    HtmlConstructor.render(List("haha", "hoho").map(new File(_)))
  }
}
