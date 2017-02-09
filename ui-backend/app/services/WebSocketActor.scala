package services

import DataTypes.{ImageRequest, ImageRequest2}
import akka.actor._
import play.api.Play.current
import play.api.libs.concurrent.Akka._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, _}
import services.Kafka.getInstance

import scala.concurrent.ExecutionContext.Implicits.global

class WebSocketActor (out: ActorRef) extends Actor {

  val actorPath = out.path.toString

  def receive = {
    case msg: String =>
      println(msg)
    // Catch all messages as generic Json and handle parsing of different potential types inside
    case msg: JsValue =>
      selectVersionFromJson(msg)
        .fold[Unit](
        image =>
          getInstance()
            .send(image.get, "./app/schemas/imageRequest.avsc")
            .onSuccess{ case _ => println(s"V1: Pushed url to kafka ${image.get.url} in Avro format") }
        ,
        image =>
          getInstance()
            .send(image.get, "./app/schemas/imageRequest2.avsc")
            .onSuccess{ case _ => println(s"V2: Pushed url & filter to kafka ${image.get.url} in Avro format") }
        )
    case _ =>
      println(s"Uncaught message type")
  }

  /**
    * The schema version to be used based on the client JSON payload
    */
  private def selectVersionFromJson(msg: JsValue): Either[JsResult[ImageRequest], JsResult[ImageRequest2]] =
    if ((msg \ "filter").asOpt[String].isEmpty)
      Left(Json.fromJson(msg)(Json.reads[ImageRequest]))
    else
      Right(Json.fromJson(msg)(Json.reads[ImageRequest2]))

}

object WebSocketActor {
  def push(actorPath: String, payload: JsValue) =  system.actorSelection(actorPath) ! payload
  def props(out: ActorRef) = Props(new WebSocketActor(out))
}

