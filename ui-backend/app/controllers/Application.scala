package controllers

import com.google.inject.Inject
import play.api.libs.streams._
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.mvc._
import play.api.libs.json._
import services.WebSocketActor
import DataTypes._
import play.api.mvc.WebSocket.MessageFlowTransformer

class Application @Inject() (implicit system: ActorSystem, materializer: Materializer) extends Controller {

//  implicit val inEventFormat = Json.format[ImageUrl]
//  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[ImageUrl, JsValue]

  def webSocket = WebSocket.accept[JsValue, JsValue] { request =>
    ActorFlow.actorRef(out => WebSocketActor.props(out))
  }

}
