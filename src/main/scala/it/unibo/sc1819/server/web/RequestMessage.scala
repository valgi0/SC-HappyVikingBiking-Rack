

package it.unibo.sc1819.server.web

/**
  * This class is used for define the message accepted to the RouterRequest.
  *
  */

object RequestMessage {

  sealed trait JsonRequest

  case class LockMessage(rackID:String, bikeID:String) extends JsonRequest

  case class ErrorLogMessage(rackID:String, errorMessage:String) extends JsonRequest
}



