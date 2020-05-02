package utils

import java.io.File

import algebras.CrawlerFileSystem
import org.http4s.Uri
import CrawlerFileSystem.defaultDir

object UriUtils {
  implicit class UriOps(val uri: Uri) {
    val directory = s"$defaultDir/${uri.host.get}"

    private def extractLast: String =
      uri.path.split("/").last

    def toJsPath: File =
      new File(s"$directory/js/$extractLast")

    def toCssPath: File =
      new File(s"$directory/css/$extractLast")

    def toIndex: File =
      new File(s"$directory/index.html")
  }
}
