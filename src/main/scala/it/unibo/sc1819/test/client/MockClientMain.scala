package it.unibo.sc1819.test.client

import io.vertx.scala.core.Vertx

object MockClientMain extends App {

  val TOPIC_ADDRESS = "HealthCare"

  val vertx = Vertx.vertx()

  vertx.deployVerticle(new WebVerticle())

  vertx.deployVerticle(new MockSensorVerticle())

}
