package it.unibo.sc1819.server

import io.vertx.core.http.HttpMethod
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import io.vertx.scala.core.http.HttpServerOptions
import io.vertx.scala.ext.web.handler.BodyHandler
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.unibo.sc1819.server.api.API.{LockBikeAPI, UnlockBikeAPI}
import it.unibo.sc1819.server.api.ResponseMessage.{BikeIDMessage, Error, Message}
import it.unibo.sc1819.server.api.{API, ResponseStatus, RouterResponse}
import it.unibo.sc1819.server.web.WebClient
import it.unibo.sc1819.util.messages.Topics
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.read
import it.unibo.sc1819.server.web.LOCK_API_PATH
import it.unibo.sc1819.server.web.RequestMessage.{ErrorLogMessage, LockMessage}

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

  def apply(rackID:String, vertx: Vertx, bracketList:List[String], remoteServerIP:String, remoteServerPort:Int, port:Int): ServerVerticle =
    new ServerVerticleImpl(rackID,vertx, bracketList, remoteServerIP, remoteServerPort, port)

  private class ServerVerticleImpl(val rackID:String, val vertxContext:Vertx, bracketList: List[String],
                                   val remoteServerIP:String,val remoteServerPort:Int,
                                   val port:Int) extends ServerVerticle {

    val bracketMap = scala.collection.mutable.Map(
      bracketList.map(ipAddress => {
        val emptyString:Option[String] = None
        ipAddress -> emptyString
      }): _*)
    val bracketQueue: mutable.Queue[String] = mutable.Queue()
    val eventBus = vertxContext.eventBus
    val webClient = WebClient(vertxContext)
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
      println("IP DELLA RICHIESTA E: " + ipAddress)
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
      println(bracketMap.toList)
      notifyRemoteServerLock(bikeID, bracketList.indexOf(ipAddress), 0)
    }

    /**
      * Notify the remote server that a bike has been locked
      * @param bikeID the bike id to be notified to remote server of lock
      * @param position the position on which the rack is setted
      * @param tries the tires effectuated by the remote api
      */
    private def notifyRemoteServerLock(bikeID:String, position:Int, tries:Int):Unit = {
      val newtries = tries + 1
      if(newtries < web.MAX_TRIES) {
        webClient.executeAPICall(remoteServerIP, HttpMethod.PUT, LOCK_API_PATH,remoteServerPort,
          web.handlerToOnlyFailureConversion(_ => notifyRemoteServerLock(bikeID, position,newtries)),
          Some(LockMessage(rackID, bikeID, position)))
      } else {
        webClient.executeAPICall(remoteServerIP, HttpMethod.PUT, LOCK_API_PATH,remoteServerPort,
          web.handlerToOnlyFailureConversion(_ => definitiveErrorHandler()), Some(LockMessage(rackID, bikeID, position)))
      }

    }

    /**
      * Error handler to be called when the for ten or more times the remote server does not respond.
      */
    private def definitiveErrorHandler():Unit = {
      webClient.executeAPICall(remoteServerIP, HttpMethod.POST, web.ERROR_PATH,remoteServerPort,
        web.handlerToOnlyFailureConversion(_ => {}), Some(ErrorLogMessage(rackID, web.ERROR_LOG_MESSAGE)))
    }
  }


}
