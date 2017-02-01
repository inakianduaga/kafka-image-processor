package services

import akka.actor._
import play.api.libs.concurrent.Akka._
import play.api.libs.json._
import play.api.Play.current
import DataTypes.ImageUrl
import Kafka.getInstance
import scala.concurrent.ExecutionContext.Implicits.global

class WebSocketActor (out: ActorRef) extends Actor {

  val actorPath = out.path.toString

  def receive = {
    case msg: String =>
      println(msg)
    // Catch all messages as generic Json and handle parsing of different potential types inside
    case msg: JsValue =>
      Json.fromJson(msg)(Json.reads[ImageUrl]).foreach(image => {
        println(s"received ${image.url}")
        getInstance().send(image.url).onSuccess{ case _ => println(s"Pushed url to kafka ${image.url}") }
      })
    case _ =>
      println(s"Uncaught message type")
  }

}

object WebSocketActor {
  def push(actorPath: String, payload: JsValue) =  system.actorSelection(actorPath) ! payload
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

