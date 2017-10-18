package neko.services

import java.time.Instant

import io.circe.parser._
import io.circe.syntax._
import fs2._
import fs2.async.mutable.{Queue, Topic}
import neko.models.chat.Message
import org.http4s._
import org.http4s.dsl._
import org.http4s.server.websocket._
import org.http4s.websocket.WebsocketBits._
import org.slf4j.LoggerFactory

object Chat {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def apply()(implicit strategy: Strategy) = {
    val topic = async.topic[Task, WebSocketFrame](Text("connected")).unsafeRun()
    HttpService {
      case GET -> Root / "chat" =>
        val msgQueue = async.unboundedQueue[Task, WebSocketFrame].unsafeRun()
        WS(topic.subscribe(10).drop(1) merge msgQueue.dequeue, handleFrame(topic, msgQueue))
    }
  }

  def handleFrame[F[_]](topic: Topic[F, WebSocketFrame], msgQueue: Queue[F, WebSocketFrame]): Sink[F, WebSocketFrame] = _.evalMap {
    case Text(text, _) =>
      (for {
        json <- parse(text)
        message <- json.as[Message]
      } yield message) match {
        case Left(error) =>
          logger.info(s"invalid message: $text")
          logger.debug(s"$error")
          msgQueue.enqueue1(Text("invalid message"))
        case Right(message) =>
          logger.info(s"received message from: ${message.author}, action: ${message.action}, payload: ${message.payload}")
          (topic.publish1 _ compose transformMessage _ compose stampMessage _) (message)
      }
    case frame =>
      logger.info(s"Unhandled WebSocket Frame, ${frame.opcode}")
      msgQueue.enqueue1(Text(s"Cannot handle the frame ${frame.opcode}"))
  }

  def stampMessage(msg: Message): Message = msg.copy(timestamp = Some(Instant.now.toEpochMilli))

  def transformMessage(msg: Message): WebSocketFrame = Text(msg.asJson.noSpaces)
}