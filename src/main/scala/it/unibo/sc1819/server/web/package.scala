package it.unibo.sc1819.server

import io.vertx.core.{AsyncResult}
import io.vertx.core.buffer.Buffer
import io.vertx.scala.ext.web.client.HttpResponse

package object web {

  val OK_CODE = 200

  val LOCK_API_PATH = "/api/delivebike"
  //TODO  INSERT A LOG
  val ERROR_PATH = "/api/rackerror"
  val MAX_TRIES = 10
  val ERROR_LOG_MESSAGE = "COULD NOT COMMUNICATE WITH THE REMOTE SERVER FOR " + MAX_TRIES +
    "TIMES, NOW IN ERROR STATE"

  /**
    * A converter that merges together two handlers
    * @param onSuccess the handler if the status code is 200
    * @param onFailure the handler for every other situation.
    * @return a AsyncResult => Unit method
    */
  def handlerToSuccessFailureConversion(onSuccess:Option[String] => Unit,
                                                 onFailure:Option[String] => Unit):
  AsyncResult[HttpResponse[Buffer]] => Unit = ar => {
    if(ar.succeeded()) {
      if(ar.result().statusCode() == OK_CODE) {
        onSuccess(ar.result().bodyAsString())
      } else {
        onFailure(ar.result().bodyAsString())
      }
    } else {
      onFailure(Some(ar.cause().getMessage))
    }
  }

  def handlerToOnlyFailureConversion(onFailure:Option[String] => Unit):
  AsyncResult[HttpResponse[Buffer]] => Unit = ar => {
    if(ar.succeeded()) {
      if(ar.result().statusCode() != OK_CODE) {
        println(ar.result().statusCode())
        println(ar.result().bodyAsString)
        onFailure(ar.result().bodyAsString())
      }
    } else {
      println(ar.result().statusCode())
      println(ar.result().bodyAsString)
      onFailure(Some(ar.cause().getMessage))
    }
  }

}
