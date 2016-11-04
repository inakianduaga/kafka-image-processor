package controllers

import play.api.mvc._
import play.api.libs.json._
import services.WebSocketActor

object Application extends Controller {

  def webSocket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out => {
      // Assing a new websocket ID to this connection
      WebSocketActor.addActorPath(id = scala.util.Random.nextInt(1000000).toString(), out.path.toString)
      WebSocketActor.props(out)
    }
  }

}
