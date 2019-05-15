package it.unibo.sc1819.server

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.http.HttpServerOptions
import io.vertx.scala.ext.web.handler.BodyHandler
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.unibo.sc1819.server.api.API.{LockBikeAPI,UnlockBikeAPI}
import it.unibo.sc1819.server.api.ResponseMessage.{BikeIDMessage, Error, Message}
import it.unibo.sc1819.server.api.{API, ResponseStatus, RouterResponse}
import it.unibo.sc1819.util.messages.Topics
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.read

import scala.collection.mutable

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

object ServerVerticle {

  def apply(vertx: Vertx, bracketList:List[String], remoteServerIP:String, port:Int): ServerVerticle =
    new ServerVerticleImpl(vertx, bracketList, remoteServerIP, port)

  private class ServerVerticleImpl(val vertxContext:Vertx, bracketList: List[String],
                                   val remoteServerIP:String, val port:Int) extends ServerVerticle {

    val bracketMap = scala.collection.mutable.Map(
      bracketList.map(ipAddress => {
        val emptyString:Option[String] = None
        ipAddress -> emptyString
      }): _*)
    val bracketQueue: mutable.Queue[String] = mutable.Queue()
    val eventBus = vertxContext.eventBus
    implicit val formats: DefaultFormats.type = DefaultFormats

    override def start(): Unit = {
      eventBus.consumer[String](Topics.LOCK_SERVER_TOPIC).handler(message => handleMessageLock(message.body()))

      val router = Router.router(vertxContext)
      router.route.handler(BodyHandler.create())
      API.values.map({
        case api@LockBikeAPI => api.asRequest(router, handleRestAPILock)
        case api@UnlockBikeAPI => api.asRequest(router, handleRestAPIUnlock)
        case _ => println("API IGNORATA")
        })

      val options = HttpServerOptions()
      options.setCompressionSupported(true)
        .setIdleTimeout(10000)

      vertxContext.createHttpServer(options)
        .requestHandler(router.accept _)
        .listen(port)
    }

    override def handleMessageLock(bracketLockedIP: String): Unit = {
      bracketQueue += bracketLockedIP
      println(bracketQueue)
    }

    override def handleRestAPILock(routingContext: RoutingContext, response: RouterResponse): Unit = {
      val ipAddress = routingContext.request().remoteAddress().host()
      val bikeID = read[BikeIDMessage](routingContext.getBodyAsString().get).bikeID
      bracketQueue.dequeueFirst(_.equals(ipAddress)) match {
        case Some(element) => confirmCorrectLockAndNotifyServer(element, bikeID)
          response.sendResponse(Message("Tutto ok"))
        case None => errorHandler(response, "No Bike found")

      }
    }


    override def handleRestAPIUnlock(routingContext: RoutingContext, response: RouterResponse): Unit = {
      val bikeID = read[BikeIDMessage](routingContext.getBodyAsString().get).bikeID
      bracketMap find (_._2.contains(bikeID)) match {
        case Some(bracketEntry) => sendUnlockMessage(bracketEntry._1)
          response.sendResponse(Message("Tutto ok"))
        case _ => errorHandler(response,"BIKE IS NOT PRESENT INSIDE THE RACK")
      }
    }

    private def sendUnlockMessage(brackToUnlock:String) = {
      eventBus.publish(Topics.UNLOCK_WORKER_TOPIC, brackToUnlock)
    }

    private def errorHandler(response:RouterResponse, message:String) =
      response.setError(ResponseStatus.NotFound, Some(message))
        .sendResponse(Error())

    /**
      * Remove the bike from the queue and notify the remote server.
      * @param ipAddress the ip address of the bracket.
      * @param bikeID the bike locked to the bracket.
      */
    private def confirmCorrectLockAndNotifyServer(ipAddress: String, bikeID:String):Unit = {
      bracketMap.put(ipAddress, Some(bikeID))
      notifyRemoteServerLock(bikeID)
    }

    /**
      * Notify the remote server that a bike has been locked
      * @param bikeID the bike id to be notified to remote server of lock
      * @return
      */
    private def notifyRemoteServerLock(bikeID:String) = {
      println("Biciletta è registrata tramite chiamata, contatto il server")
    }//TODO complete this
  }


}
