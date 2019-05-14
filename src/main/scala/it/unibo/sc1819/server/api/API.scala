
package it.unibo.sc1819.server.api

import io.vertx.core.http.HttpMethod
import io.vertx.scala.ext.web.{Router, RoutingContext}

object API {

  /**
    * Trait of DiscoveryAPI.
    */
  sealed trait RestAPI {
    /**
      * Path of the RestAPI
      *
      * @return a string containing the path of RestAPI.
      */
    def path: String

    /**
      * Http method of RestAPI.
      *
      * @return an HTTP method to call.
      */
    def httpMethod: HttpMethod

    /**
      * Convert the RestAPI to a request object to register into a router.
      *
      * @param router the router on which request will be registered.
      * @param handle the handler of the request.
      * @return a Request object build from the RestAPI.
      */
    def asRequest(router: Router, handle: (RoutingContext, RouterResponse) => Unit): Request
  }

  /**
    * Lock Bike API to signal that a bike has been locked.
    */
  case object LockBikeAPI extends RestAPI {
    override def path: String = "/lockbike"

    override def httpMethod: HttpMethod = HttpMethod.POST

    override def asRequest(router: Router, handle: (RoutingContext, RouterResponse) => Unit): Request =
      POST(router, path, handle)
  }

  /**
    * Unlock Bike API to signal that a bike has been locked.
    */
  case object UnlockBikeAPI extends RestAPI {
    override def path: String = "/unlockbike"

    override def httpMethod: HttpMethod = HttpMethod.POST

    override def asRequest(router: Router, handle: (RoutingContext, RouterResponse) => Unit): Request =
      POST(router, path, handle)
  }

  //TODO IMPLEMENT API FOR REMOTE UNLOCKING

  /**
    * Values static method, analog of the Java's enumeration one.
    * @return a set containing all the object extended from RestAPI trait
    */
  def values:Set[RestAPI] = Set(LockBikeAPI)

}
