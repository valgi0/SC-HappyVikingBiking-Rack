package it.unibo.sc1819.server

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ext.web.RoutingContext
import it.unibo.sc1819.server.api.RouterResponse

/**
  * This trait will handle the server side implementation of the rack: will expose the necessary REST API
  * and manage high level abstraction for the brackets installed on the rack.
  */
trait ServerVerticle extends ScalaVerticle {

  /**
    * This handler will be called when the Verticle receive a message from a Bracket.
    * @param bracketLockedIP the IP of the bracket which has been closed.
    */
  def handleMessageLock(bracketLockedIP:String)

  /**
    * This handler will be called when a Bike that has been locked contact the server.
    * @param routingContext the routing context of the request.
    * @param response the response object to send.
    */
  def handleRestAPILock(routingContext: RoutingContext, response:RouterResponse)

  /**
    * This handler will be called when the Remote server unlock a bike inside the rack
    * @param routingContext the routing context of the request.
    * @param response the response object to send
    */
  def handleRestAPIUnlock(routingContext: RoutingContext, response:RouterResponse)

}
