package it.unibo.sc1819.util.messages

/**
  * Trait to be extended by all event bus messages
  */
sealed trait EventBusMessage {

}

/**
  * Message for notifying the worker that a specific bracket has been occupied.
  * @param sourceIpAddress the ip address of the bracket that has locked the bike
  */
case class LockBikeMessage(sourceIpAddress: String) extends EventBusMessage

/**
  * Object that will contain all the topics inside the rack.
  */
object Topics {
  val WORKER_TOPIC = "worker"

}
