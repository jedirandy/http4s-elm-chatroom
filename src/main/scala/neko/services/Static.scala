package neko.services

import java.io.File

import fs2._
import fs2.interop.cats._
import org.http4s._
import org.http4s.dsl._
import org.slf4j.LoggerFactory

object Static {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def apply()(implicit strategy: Strategy) = HttpService {
    case GET -> Root =>
      logger.info("GET /")
      StaticFile.fromFile(new File("index.html")).getOrElseF(NotFound())
    case GET -> Root / path if List(".js", ".css").exists(path.endsWith) =>
      logger.info(s"GET $path")
      StaticFile.fromFile(new File(path)).getOrElseF(NotFound())
  }
}
