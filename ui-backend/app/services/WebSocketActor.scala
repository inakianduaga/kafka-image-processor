package services

import akka.actor._
import play.api.libs.concurrent.Akka._
import play.api.libs.json.JsValue
import play.api.Play.current

class WebSocketActor(out: ActorRef) extends Actor {

  val actorPath = out.path.toString

  def receive = {
    case msg: String =>
      println(msg)
    case msg: JsValue =>
      // TODO: Hook webSocket to Kafka stream so we can push whatever we get from the webSocket upstream
      println(s"Message: $msg for webSocketId: $actorPath)")
  }

}

object WebSocketActor {

  def push(actorPath: String, payload: JsValue) =  system.actorSelection(actorPath) ! payload
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

