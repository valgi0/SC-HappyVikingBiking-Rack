package it.unibo.sc1819.server

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.{Router, RoutingContext}
import it.unibo.sc1819.server.api.API.LockBikeAPI
import it.unibo.sc1819.server.api.{API, BikeIDMessage, RouterResponse}
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

    override def start(): Unit = {
      eventBus.consumer[String](Topics.LOCK_SERVER_TOPIC).handler(message => handleMessageLock(message.body()))

      val router = Router.router(vertxContext)
      API.values.map({
        case api@LockBikeAPI => api.asRequest(router, handleRestAPILock)
        case _ => println("API IGNORATA")
        })

      vertxContext.createHttpServer()
        .requestHandler(router.accept _)
        .listen(port)
    }

    override def handleMessageLock(bracketLockedIP: String): Unit = bracketQueue += bracketLockedIP


    override def handleRestAPILock(routingContext: RoutingContext, response: RouterResponse): Unit = {
      val ipAddress = routingContext.request().remoteAddress().host()
      implicit val formats: DefaultFormats.type = DefaultFormats
      val bikeID = read[BikeIDMessage](routingContext.getBodyAsString().get).bikeID
      bracketQueue.dequeueFirst(_.equals(ipAddress)) match {
        case Some(element) => confirmCorrectLockAndNotifyServer(element, bikeID)
        case None => println("ERRORE GRAVISSIMO")
      }
    }


    override def handleRestAPIUnlock(routingContext: RoutingContext, response: RouterResponse): Unit = ???

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
      * @param bikeID
      * @return
      */
    private def notifyRemoteServerLock(bikeID:String) = {
      println("Biciletta è registrata tramite chiamata, contatto il server")
    }//TODO complete this
  }


}
