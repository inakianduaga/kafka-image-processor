package services

import akka.actor._
import play.api.libs.concurrent.Akka._
import play.api.libs.json.JsValue
import play.api.mvc.{ AnyContent, Request}
import WebSocketActor._

class WebSocketActor(out: ActorRef) extends Actor {

  val actorPath = out.path.toString
  val webSocketId: Option[String] = getWebSocketIdByActor(actorPath)

  def receive = {
    case msg: String =>
      println(msg)
    case msg: JsValue =>
      // TODO: Hook webSocket to Kafka stream so we can push whatever we get from the webSocket upstream
      println(s"Message: ${msg} for webSocketId: ${webSocketId})")
  }
}

object WebSocketActor {

  /**
    * Maps of webSocketIds to Akka actorPaths
    */
  var wsActorMapping: Map[String, String] = Map()

  def addActorPath(id: String, actorPath: String) = wsActorMapping += (id -> actorPath)

  def getActorPath(id: String) = wsActorMapping.get(id)

  def getWebSocketIdByActor(actorPath: String): Option[String] = wsActorMapping.find(_._2 == actorPath).map(_._1)

  def pushUpdate = (webSocketId: String, payload: JsValue) =>
    WebSocketActor
      .getActorPath(webSocketId)
      .map(system.actorSelection(_))
      .map(_ ! payload)

  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

