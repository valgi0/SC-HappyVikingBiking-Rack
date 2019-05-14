

package it.unibo.sc1819.server.api

/**
  * This class is used for define the message accepted to the RouterResponse.
  *
  */

sealed trait JsonResponse

case class Message(message: String) extends JsonResponse


