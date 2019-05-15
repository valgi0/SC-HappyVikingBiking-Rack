package it.unibo.sc1819.test.client

import io.vertx.lang.scala.ScalaVerticle

class MockSensorVerticle extends ScalaVerticle {

  override def start(): Unit = {
    val vertx = MockClientMain.vertx
    val eventBus = vertx.eventBus()

    vertx.setPeriodic(5000, _ => {
      eventBus.publish(MockClientMain.TOPIC_ADDRESS, "Yay! Someone got lumbago! Notify the server!")
    })
  }

}
