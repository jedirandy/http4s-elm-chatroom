package neko

import cats.implicits._
import fs2.Strategy
import neko.services.{Chat, Static}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.global

object Main extends StreamApp {
  lazy val logger = LoggerFactory.getLogger(this.getClass)

  val services = List(
    Static()(Strategy.fromExecutionContext(global)),
    Chat()(Strategy.fromFixedDaemonPool(Runtime.getRuntime.availableProcessors))
  ).foldMap(identity)

  override def stream(args: List[String]) = {
    BlazeBuilder
      .bindHttp(sys.env.get("NEKO_PORT").map(_.toInt).getOrElse(8080))
      .mountService(services)
      .withWebSockets(true)
      .serve
  }
}
