package services

import akka.actor._
import play.api.libs.concurrent.Akka._
import play.api.libs.json._
import play.api.Play.current
import DataTypes.ImageRequest
import Kafka.getInstance
import scala.concurrent.ExecutionContext.Implicits.global

class WebSocketActor (out: ActorRef) extends Actor {

  val actorPath = out.path.toString

  def receive = {
    case msg: String =>
      println(msg)
    // Catch all messages as generic Json and handle parsing of different potential types inside
    case msg: JsValue =>
      Json.fromJson(msg)(Json.reads[ImageRequest]).foreach(image => {
        println(s"received ${image.url}")
        // Send regular url
        getInstance().send(image.url).onSuccess{ case _ => println(s"Pushed url to kafka ${image.url}") }
        // Send Avro url
        getInstance().send(ImageRequest(image.url)).onSuccess{ case _ => println(s"Pushed url to kafka ${image.url} in Avro format") }

        // TODO: Add logic to send either V1 avro or V2 avro depending on the filter
      })
    case _ =>
      println(s"Uncaught message type")
  }

}

object WebSocketActor {
  def push(actorPath: String, payload: JsValue) =  system.actorSelection(actorPath) ! payload
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

