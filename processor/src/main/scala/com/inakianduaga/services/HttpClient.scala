package com.inakianduaga.services

import play.api.libs.ws.ning._
import play.api.libs.json.JsValue
import play.api.libs.ws._
import scala.concurrent.Future

object HttpClient {

  val client = NingWSClient(NingWSClientConfig())

  def get(url:String): Future[WSResponse] = HttpClient.client.url(url).get()
}
