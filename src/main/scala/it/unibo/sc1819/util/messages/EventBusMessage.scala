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
  * The name of the channel is the name of the component which will receive the message.
  */
object Topics {
  val LOCK_WORKER_TOPIC = "lock_worker"
  val LOCK_SERVER_TOPIC = "lock_server"
  val UNLOCK_WORKER_TOPIC = "unlock_WORKER"
}
