package native

import cats.effect.Concurrent
import cats.syntax.functor._
import fs2._
import fs2.concurrent.Queue
import fs2.io.tcp.Socket
import native.HttpSocket.HttpMessage

trait HttpSocket[F[_]] {
  def read: Stream[F, HttpMessage]
  def write(in: HttpMessage): F[Unit]
}

object HttpSocket {
  case class HttpMessage(body: String)

  /**
    * Since it is only necessary to analyze the path to the resource,
    * we have two possibilities:
    *
    * 1) Read exactly `numBytes` from the peer in a single chunk
    *
    * 2) Use `reads`, that is potentially infinite. That stream emits infinite number of bytes until EOF.
    * We can match them and then use shutdownInput() of [[java/nio/channels/AsynchronousSocketChannel.java]].
    *
    * This think does following:
    * Once shutdown for reading then further reads on the channel will return -1, the end-of-stream indication.
    *
    * 3) We can read all bytes from socket and write bytes in a concurrent stream,
    * so that the client throws us an EOF after receiving message.
    */
  def apply[F[_]: Concurrent](socket: Socket[F]): F[HttpSocket[F]] =
    Queue.unbounded[F, HttpMessage].map { queue =>
      new HttpSocket[F] {
        def read: Stream[F, HttpMessage] = {
          val concurrentWrite =
            queue.dequeue
              .map(_.body)
              .through(text.utf8Encode[F])
              .through(socket.writes()) ++ Stream.eval(socket.endOfOutput)

          socket
            .reads(2048) // read up to 2048 in a chunk
            .through(text.utf8Decode)
            .map(HttpMessage)
            .concurrently(concurrentWrite)
        }

        def write(in: HttpMessage): F[Unit] = queue.enqueue1(in)
      }
    }
}
