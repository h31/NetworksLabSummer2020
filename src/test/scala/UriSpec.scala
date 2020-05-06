import java.io.File

import algebras.Extractor.{CssUri, JsUri}
import org.http4s.Uri
import org.scalatest.{FlatSpec, Matchers}

class UriSpec extends FlatSpec with Matchers {

  "UriUtils" should "convert URI to local path's" in {
    val url1 = Uri.unsafeFromString("https://dr.habracdn.net/habrcom/javascripts/1588261330/libs/jquery-1.8.3.min.js")
    val url2 = Uri.unsafeFromString("https://dr.habracdn.net/habrcom/styles/1588261330/main.bundle.css")

    JsUri(url1).toPath shouldBe new File("./files/js/jquery-1.8.3.min.js")
    CssUri(url2).toPath shouldBe new File("./files/css/main.bundle.css")
  }
}
