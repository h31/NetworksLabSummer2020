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
  def readFile(path: File): F[HtmlContent]
  def createDirectories: F[Unit]
  def scan(fn: File => Boolean): F[List[File]]
}

final class CrawlerFileSystem[F[_]: Sync] private (
      implicit val ctx: ContextShift[F]
    , blocker: Blocker
) extends FileSystem[F] {

  def createDirectories: F[Unit] = {
    import CrawlerFileSystem.defaultDir

    val dirs = List("css", "js", "img")

    def exists: F[Option[Unit]] =
      file.exists[F](blocker, defaultDir.toPath).map {
        case true  => Some(())
        case false => None
      }

    def deleteRecursively(file: File): F[Boolean] = {
      def childrenOf(file: File): List[File] = Option(file.listFiles()).getOrElse(Array.empty).toList
      @annotation.tailrec
      def loop(files: List[File]): Boolean = files match {
        case Nil ⇒ true
        case child :: parents if child.isDirectory && child.listFiles().nonEmpty ⇒
          loop((childrenOf(child) :+ child) ++ parents)
        case fileOrEmptyDir :: rest ⇒
          fileOrEmptyDir.delete()
          loop(rest)
      }

      Sync[F].delay {
        if (!file.exists()) false
        else loop(childrenOf(file) :+ file)
      }
    }

    def deleteIfExists: File => F[Unit] =
      file =>
        exists.flatMap {
          _.fold(Sync[F].pure(())) { _ =>
            deleteRecursively(file).void
          }
        }

    def createDir(f: File): F[Path] =
      file.createDirectory[F](blocker, f.toPath)

    deleteIfExists(defaultDir) >> createDir(defaultDir) >> dirs
      .traverse(dir => createDir(new File(s"$defaultDir/$dir")))
      .void
  }

  def writeFile(path: File, content: HtmlContent): Stream[F, Unit] =
    Stream
      .emit(content.value)
      .through(text.utf8Encode[F])
      .through(io.file.writeAll(path.toPath, blocker))
      .handleErrorWith(_ => Stream.empty)

  def scan(fn: File => Boolean): F[List[File]] =
    Sync[F].delay(
        CrawlerFileSystem.defaultDir
        .listFiles()
        .filter(fn)
        .toList
    )

  def readFile(path: File): F[HtmlContent] =
    file
      .readAll[F](path.toPath, blocker, 1024)
      .through(text.utf8Decode[F])
      .compile
      .toList
      .map(_.mkString)
      .map(HtmlContent(_))
}

object CrawlerFileSystem {
  import FileImplicitUtils._
  val defaultDir    = new File("./files")
  val errorPagePath = new File("./resources/static/err.html")
  val indexPagePath = new File(s"./$defaultDir/index.html")

  def withDefaultPath: File => File = defaultDir / _.getName

  def apply[F[_]: Sync](implicit C: ContextShift[F], B: Blocker): F[FileSystem[F]] =
    Sync[F].pure[FileSystem[F]](new CrawlerFileSystem[F]).flatTap(_.createDirectories)
}

object FileImplicitUtils {
  implicit class RichFile(file: File) extends File(file.getName) {
    def /(other: String): File = new File(file.getAbsolutePath + s"/$other")
  }
}
