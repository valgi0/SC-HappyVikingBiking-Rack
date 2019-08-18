package it.unibo.sc1819.main

import io.vertx.scala.core.Vertx
import it.unibo.sc1819.server.ServerVerticle
import it.unibo.sc1819.worker.WorkerVerticle
import it.unibo.sc1819.worker.bracket.PhysicLayerMapper
import org.rogach.scallop.{ScallopConf, ScallopOption}

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
 val remoteaddress: ScallopOption[String] = opt[String]()
 val rackname: ScallopOption[String] = opt[String]()
 val remoteport: ScallopOption[Int] = opt[Int]()
 val serveraddress: ScallopOption[String] = opt[String]()
 val serverport: ScallopOption[Int] = opt[Int]()
 val ip_brackets_pinlist: ScallopOption[String] = opt[String]()
 verify()
}

object Main extends App {

 val conf = new Conf(args)
 var remoteaddress = DEFAULT_REMOTE_SERVER_IP
 var remoteport = DEFAULT_REMOTE_SERVER_PORT
 var serveraddress = DEFAULT_RACK_SERVER_IP
 var serverport = DEFAULT_RACK_SERVER_PORT
 var ip_brackets_pinlist = DEFAULT_IP_BRACKET_CONF
 var rackname = DEFAULT_RACK_NAME

 if (conf.remoteaddress.supplied) remoteaddress = conf.remoteaddress()
 if (conf.remoteport.supplied) remoteport = conf.remoteport()
 if (conf.serveraddress.supplied) serveraddress = conf.serveraddress()
 if (conf.serverport.supplied) serverport = conf.serverport()
 if (conf.ip_brackets_pinlist.supplied) ip_brackets_pinlist = conf.ip_brackets_pinlist()
 if (conf.rackname.supplied) rackname = conf.rackname()

 val racketsConfiguration = ip_brackets_pinlist.split(DEFAULT_BRACKETS_SEPARATOR)
   .map(entry => {
    val ipAddress = entry.split(DEFAULT_IP_BRACKET_SEP)(0)
    val pinList = entry.split(DEFAULT_IP_BRACKET_SEP)(1).split(DEFAULT_PIN_SEPARATOR).map(_.toInt)
    (ipAddress, PhysicLayerMapper(pinList(0), pinList(1), pinList(2)))
   }).toList

 val racketList = ip_brackets_pinlist.split(DEFAULT_BRACKETS_SEPARATOR)
   .map(_.split(DEFAULT_IP_BRACKET_SEP)(0)).toList


 val vertxContext = Vertx.vertx
 //val racketsConfiguration = List(("192.168.1.155", PhysicLayerMapper(6, 5, 4)))
 //val racketList = List("192.168.1.155")


 val workerVerticle = WorkerVerticle(vertxContext, racketsConfiguration)


 val serverVerticle = ServerVerticle(rackname,vertxContext, racketList, remoteaddress, remoteport, serverport)

 vertxContext.deployVerticle(serverVerticle)

 vertxContext.deployVerticle(workerVerticle)

}
