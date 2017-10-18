package neko.models.chat

import org.scalatest.{FlatSpec, Matchers}
import io.circe.syntax._
import io.circe.literal._

class MessageSpec extends FlatSpec with Matchers {
  "json encoder" should "encode Message to JSON" in {
    val json = Message(Action.Join, "user", "message", Some(100L)).asJson
    json.noSpaces shouldEqual """{"action":"Join","author":"user","payload":"message","timestamp":100}"""
  }
  "json decoder" should "decode JSON to Message" in {
    val message = json"""{"action":"Join","author":"user","payload":"message","timestamp":100}""".as[Message]
    message shouldEqual Right(Message(Action.Join, "user", "message", Some(100L)))
  }
}
