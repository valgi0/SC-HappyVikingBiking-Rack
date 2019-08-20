package it.unibo.sc1819.server.web

import io.vertx.core.AsyncResult
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.client.HttpResponse
import it.unibo.sc1819.server.web.RequestMessage.JsonRequest
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write


/**
  * A generic web client used for remote call via vertx
  */
trait WebClient {

  /**
    * Execute a remote api call
    * @param remoteServer the remote server to contact
    * @param method the http method on which the request will be sent
    * @param remotePath the remote server resource path
    * @param port the port on which the server is listening
    * @param body the body of the request if present
    * @param handler the handler of the response
    */
  def executeAPICall(remoteServer:String, method:HttpMethod, remotePath:String, port:Int,
                     handler: AsyncResult[HttpResponse[Buffer]] => Unit,  body:Option[JsonRequest] = None)
}

object WebClient {
  implicit val formats: DefaultFormats.type = DefaultFormats

  def apply(context:Vertx): WebClient = new WebClientImpl(context)

  private class WebClientImpl(vertxContext: Vertx) extends WebClient {

    val client = io.vertx.scala.ext.web.client.WebClient.create(vertxContext)

    override def executeAPICall(remoteServer: String, method: HttpMethod, remotePath: String, port: Int,
                                handler: AsyncResult[HttpResponse[Buffer]] => Unit,
                                body: Option[JsonRequest] = None): Unit = {
      println("API ESEFUITA")
      method match  {
        case HttpMethod.GET => client.get(port, remoteServer, remotePath).send(handler(_))
        case HttpMethod.POST if body.isDefined => client.post(port, remoteServer, remotePath)
          .putHeader("Content-Type", "application/json")
          .sendBuffer(body.get, handler(_))
        case HttpMethod.POST => client.post(port, remoteServer, remotePath).send(handler(_))
        case HttpMethod.PUT if body.isDefined => {
          println("Parte CHIAMATA PUT a " + remoteServer + ":" + port + "/" + remotePath)
          client.put(port, remoteServer, remotePath)
            .putHeader("Content-Type", "application/json")
            .sendBuffer(body.get, handler(_))
        }
        case _ =>
      }
    }

  }

  /**
    * Implicit used to convert a body object into a buffer to make serialization great again
    * @param body the body to be convert into a buffer
    * @return a Buffer object, so it's everything fine for JS and Java
    */
  implicit def bodyToBuffer(body:JsonRequest):Buffer = Buffer.buffer(write(body))
}

