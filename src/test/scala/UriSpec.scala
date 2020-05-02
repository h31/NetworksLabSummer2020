import java.io.File

import org.http4s.Uri
import org.scalatest.{FlatSpec, Matchers}

class UriSpec extends FlatSpec with Matchers {

  "UriUtils" should "convert URI to local path's" in {
    val url1 = Uri.unsafeFromString("https://dr.habracdn.net/habrcom/javascripts/1588261330/libs/jquery-1.8.3.min.js")
    val url2 = Uri.unsafeFromString("https://dr.habracdn.net/habrcom/styles/1588261330/main.bundle.css")

    import utils.UriUtils._

    url1.toIndex shouldBe new File("./files/dr.habracdn.net/index.html")
    url1.toJsPath shouldBe new File("./files/dr.habracdn.net/js/jquery-1.8.3.min.js")

    url2.toCssPath shouldBe new File("./files/dr.habracdn.net/css/main.bundle.css")
  }
}
