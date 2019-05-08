package it.unibo.sc1819.test.server

import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.Router

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * Mock server main to be used for vertx understanding purpouse
  */
object MockServerMain extends App {

  var serverContatctedTime = 0

  val router = Router.router(Vertx.vertx())

  router.route("/lumbago").handler( routingContext => {
    serverContatctedTime = serverContatctedTime + 1
   routingContext.response().end("Server has been lumbagoed " + serverContatctedTime + " times")
  })


  Vertx.vertx()
    .createHttpServer()
    .requestHandler(router)
    .listenFuture(8080).onComplete{
    case Success(result) => println("Server is now listening!")

    case Failure(cause) => println(s"$cause")
  }

}
