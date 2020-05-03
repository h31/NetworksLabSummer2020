package algebras

import java.io.File
import java.nio.file.Path

import cats.effect.{Blocker, ContextShift, Sync}
import fs2._
import fs2.io._
import cats.syntax.traverse._
import cats.instances.list._
import cats.syntax.functor._
import cats.syntax.flatMap._
import domain.page.HtmlContent

trait FileSystem[F[_]] {
  def writeFile(path: File, content: HtmlContent): Stream[F, Unit]
  def createDirectories: F[Unit]
}

final class CrawlerFileSystem[F[_]: Sync] private (
      implicit val ctx: ContextShift[F]
    , blocker: Blocker
) extends FileSystem[F] {

  def createDirectories: F[Unit] = {
    import CrawlerFileSystem.defaultDir

    val dirs = List("css", "js", "img")

    def createDir(f: File): F[Path] =
      file.createDirectory[F](blocker, f.toPath)

    file.deleteIfExists(blocker, defaultDir.toPath) >> createDir(defaultDir) >> dirs
      .traverse(dir => createDir(new File(s"$defaultDir/$dir")))
      .void
  }

  def writeFile(path: File, content: HtmlContent): Stream[F, Unit] =
    Stream
      .emit(content.value)
      .through(text.utf8Encode[F])
      .through(io.file.writeAll(path.toPath, blocker))
}

object CrawlerFileSystem {
  import FileImplicitUtils._
  val defaultDir = new File("./files")

  def withDefaultPath: File => File = defaultDir / _.getName

  def apply[F[_]: Sync](implicit C: ContextShift[F], B: Blocker): F[FileSystem[F]] =
    Sync[F].pure[FileSystem[F]](new CrawlerFileSystem[F]).flatTap(_.createDirectories)
}

object FileImplicitUtils {
  implicit class RichFile(file: File) extends File(file.getName) {
    def /(other: String): File = new File(file.getAbsolutePath + s"/$other")
  }
}
