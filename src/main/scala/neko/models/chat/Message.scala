package neko.models.chat

import io.circe.{Decoder, Encoder, Json}

case class Message(
                    action: Action,
                    author: String,
                    payload: String,
                    timestamp: Option[Long] = None
                  )

object Message {
  implicit val encoder = Encoder.instance[Message](m => Json.obj(
    "action" -> Json.fromString(m.action.toString),
    "author" -> Json.fromString(m.author),
    "payload" -> Json.fromString(m.payload),
    "timestamp" -> Json.fromLong(m.timestamp.getOrElse(0))
  ))

  implicit val decoder = Decoder.instance[Message](hc =>
    for {
      action <- hc.downField("action").as[String].map(Action.from)
      author <- hc.downField("author").as[String]
      payload <- hc.downField("payload").as[Option[String]]
      timestamp <- hc.downField("timestamp").as[Option[Long]]
    } yield Message(action, author, payload.getOrElse(""), timestamp)
  )
}

