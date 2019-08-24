package it.unibo.sc1819.worker

import io.vertx.core.AbstractVerticle
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import it.unibo.sc1819.util.messages.Topics
import it.unibo.sc1819.worker.bracket.{PhysicLayerMapper, RackBracket}

/**
  * This Verticle is the main component of the Low level implementation of the rack
  * Receive message from both the server and the brackets and handle the communication between them
  */
trait WorkerVerticle extends ScalaVerticle {

  /**
    * Define the actions to be performed when a bracket is locked.
    * @param ipAddress the address ip of the bracket that is locked.
    */
  def onBracketLock(ipAddress:String)

  /**
    * Define the action to be executed when a unlock bracket message is received.
    * @param ipAddress the address ip of the bracket to unlock
    */
  def onBracketUnlock(ipAddress:String)
}

object WorkerVerticle {

  def apply(vertxContext:Vertx, racketsConfiguration: List[(String, PhysicLayerMapper, Option[String])]): WorkerVerticle =
    new WorkerVerticleImpl(racketsConfiguration, vertxContext)

  private class WorkerVerticleImpl(val racketsConfiguration: List[(String, PhysicLayerMapper, Option[String])],
                                   val vertxContext:Vertx) extends WorkerVerticle {
    val bracketList = racketsConfiguration.map(config => RackBracket(config._1, config._2, config._3,vertxContext))
    val eventBus = vertxContext.eventBus

    override def onBracketLock(ipAddress: String): Unit = eventBus.publish(Topics.LOCK_SERVER_TOPIC, ipAddress)

    override def onBracketUnlock(ipAddress: String): Unit =
      bracketList.find(_.ipAddress.equals(ipAddress)).get.unlockBike()


    override def start(): Unit = {
      listenForMessages(Topics.LOCK_WORKER_TOPIC, onBracketLock)
      listenForMessages(Topics.UNLOCK_WORKER_TOPIC, onBracketUnlock)
    }

    /**
      * Define a listener method to make possible listening on every channel
      * @param topic the topic on which the listener will listen
      * @param messageHandler the handler to call when a message is received.
      */
    private def listenForMessages(topic:String, messageHandler:String => Unit ):Unit = {
      eventBus.consumer[String](topic).handler(message => {
        messageHandler(message.body())
      })
    }


  }
}
