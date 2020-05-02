package algebras

import java.io.File
import java.nio.file.Path

import cats.effect.{Blocker, ContextShift, Sync}
import fs2._
import fs2.io._
import cats.syntax.functor._
import cats.syntax.flatMap._
import domain.page.HtmlContent

trait FileSystem[F[_]] {
  def writeFile(path: File, content: HtmlContent): Stream[F, Unit]
  def createDirectory: F[Path]
}

final class CrawlerFileSystem[F[_]: Sync] private (
      implicit val ctx: ContextShift[F]
    , blocker: Blocker
) extends FileSystem[F] {

  /**
    * deterministic function, that will create a directory if it does not exist
    */
  def createDirectory: F[Path] = {
    import CrawlerFileSystem.defaultDir

    exists(defaultDir).flatMap {
      _.fold(
          file.createDirectory[F](blocker, defaultDir.toPath)
      )(_ => Sync[F].pure(defaultDir.toPath))
    }
  }

  private def exists(path: File): F[Option[Unit]] =
    file.exists[F](blocker, path.toPath).map {
      case true  => Some(())
      case false => None
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
    Sync[F].pure[FileSystem[F]](new CrawlerFileSystem[F]).flatTap(_.createDirectory)
}

object FileImplicitUtils {
  implicit class RichFile(file: File) extends File(file.getName) {
    def /(other: String): File = new File(file.getAbsolutePath + s"/$other")
  }
}
