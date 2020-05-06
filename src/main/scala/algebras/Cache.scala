package algebras

import algebras.Extractor.HtmlResource
import cats.Functor
import cats.effect.Sync
import cats.effect.concurrent.Ref
import org.http4s.Uri

import scala.collection.{mutable => m}

trait Cache[F[_], K, V] {
  def get(k: K): F[Option[V]]
  def put(k: K, v: V): F[Unit]
  def all: F[List[V]]
}

private class CrawlerCache[F[_]: Functor, K, V] private (cache: Ref[F, m.Map[K, V]]) extends Cache[F, K, V] {
  def get(k: K): F[Option[V]]  = Functor[F].map(cache.get)(_.get(k))
  def put(k: K, v: V): F[Unit] = cache.update(_ += k -> v)
  def all: F[List[V]]          = Functor[F].map(cache.get)(_.values.toList)
}

object CrawlerCache {
  case class UriNode(uri: Uri)
  type Key         = UriNode
  type Value       = Set[HtmlResource]
  type Index[F[_]] = Cache[F, Key, Value]

  def acquire[F[_]: Sync: Functor]: F[Index[F]] =
    Functor[F].map(Ref.of(m.Map.empty[Key, Value]))(new CrawlerCache[F, Key, Value](_))

  def acquireOne[F[_]: Sync: Functor](start: UriNode): F[Index[F]] =
    Functor[F].map(
        Ref.of(m.Map(start -> Set.empty[HtmlResource]))
    )(new CrawlerCache[F, Key, Value](_))
}
