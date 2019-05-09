package it.unibo.sc1819.test.client

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.ext.web.client.WebClient

import scala.util.{Failure, Success}

class WebVerticle extends ScalaVerticle {

  override def start(): Unit = {
    val vertx = MockClientMain.vertx
    val webClient = WebClient.create(vertx)
    val eventBus = vertx.eventBus()

    eventBus
      .consumer[String](MockClientMain.TOPIC_ADDRESS)
      .handler( message => {
        println("Oh no, message received")
        webClient.get(8080, "192.168.1.155", "/lumbago")
          .sendFuture().onComplete{
          case Success(result) => {
            println("Server respond " + result.body().get)
          }
          case Failure(cause) => {
            println(s"$cause")
          }
        }

      })
  }
}
