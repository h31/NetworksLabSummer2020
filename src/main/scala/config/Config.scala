package config

import cats.MonadError
import cats.effect.Sync
import cats.implicits._
import pureconfig._
import pureconfig.generic.auto._ // do not remove
import pureconfig.error.ConfigReaderException

case class ServerConfig(port: Int, host: String)
case class FetchUrl(url: String)
case class FetchTime(time: Int)
case class Config(serverConfig: ServerConfig, fetchUrl: FetchUrl, fetchTime: FetchTime)

object Config {
  def load[F[_]](implicit E: MonadError[F, Throwable], S: Sync[F]): F[Config] =
    S.delay(ConfigSource.default.load[Config]).flatMap {
      _.fold(
          e => E.raiseError(new ConfigReaderException[Config](e))
        , S.pure
      )
    }
}
