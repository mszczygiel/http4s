package org.http4s
package server

import scalaz.concurrent.Task

import middleware.PushSupport._
import Status._

object MockRoute extends Http4s {

  def route(): HttpService = {
    case req: Request if req.uri.path ==  "/ping" =>
      Ok("pong")

    case req: Request if req.method == Method.POST && req.uri.path == "/echo" =>
      Task.now(Response(body = req.body))

    case req: Request if req.uri.path == "/fail" =>
      sys.error("Problem!")
      Ok("No problem...")

    /** For testing the UrlTranslation middleware */
    case req: Request if req.uri.path == "/checktranslate" =>
      import org.http4s.server.middleware.URITranslation._
      val newpath = req.attributes.get(translateRootKey)
        .map(f => f("foo"))
        .getOrElse("bar!")

      Ok(newpath)

    /** For testing the PushSupport middleware */
    case req: Request if req.uri.path == "/push" =>
      Ok("Hello").push("/ping")(req)
  }
}
