package controllers

import play.api.mvc._
import play.api.libs.json._
import services.WebSocketActor

object Application extends Controller {

  def webSocket = WebSocket.acceptWithActor[JsValue, JsValue] { request => out => {
      WebSocketActor.props(out)
    }
  }

}
