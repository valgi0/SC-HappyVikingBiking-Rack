package it.unibo.sc1819.main

import io.vertx.scala.core.Vertx
import it.unibo.sc1819.server.ServerVerticle
import it.unibo.sc1819.worker.WorkerVerticle
import it.unibo.sc1819.worker.bracket.PhysicLayerMapper

object MockScalaMain extends App {

 val vertxContext = Vertx.vertx
 val racketsConfiguration = List(("192.168.1.155", PhysicLayerMapper(6, 5, 4)))
 val racketList = List("192.168.1.155")


 val workerVerticle = WorkerVerticle(vertxContext, racketsConfiguration)

 val serverVerticle = ServerVerticle("MOCK RACK",vertxContext, racketList, "localhost", 8080, 8888)

 vertxContext.deployVerticle(serverVerticle)

 vertxContext.deployVerticle(workerVerticle)

}
