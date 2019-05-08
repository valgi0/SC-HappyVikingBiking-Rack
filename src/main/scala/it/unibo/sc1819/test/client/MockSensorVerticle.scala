package it.unibo.sc1819.test.client

import io.vertx.core.AbstractVerticle
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx

class MockSensorVerticle extends ScalaVerticle {

  override def start(): Unit = {
    val vertx = Vertx.vertx()
    val eventBus = vertx.eventBus()

    vertx.setPeriodic(1000, _ => {
      eventBus.publish("healthcare", "Yay! Someone got lumbago! Notify the server!")
    })
  }

}
