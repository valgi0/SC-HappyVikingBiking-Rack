

package it.unibo.sc1819.server.api

/**
  * This class is used for define the message accepted to the RouterResponse.
  *
  */

object ResponseMessage {

  sealed trait JsonResponse

  case class Message(message: String) extends JsonResponse

  case class BikeIDMessage(bikeID:String) extends JsonResponse

  case class Error(cause: Option[String] = None) extends JsonResponse
}



