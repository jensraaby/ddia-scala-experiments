package com.jensraaby.rest

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.param.Stats
import com.twitter.finagle.stats.StatsReceiver
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Duration}
import io.finch._

object Server extends TwitterServer {

  val api: Service[Request, Response] = new Api(statsReceiver)

  val port = flag("http.port", ":8081", "Address port to bind to, prefixed by :")
  def main(): Unit = {
    val server = Http.server
      .configured(Stats(statsReceiver))
      .withAdmissionControl.concurrencyLimit(maxConcurrentRequests = 1000, maxWaiters = 10)
      .withRequestTimeout(Duration.fromMilliseconds(500))
      .serve(port(), api)

    onExit {
      server.close()
    }

    Await.ready(adminHttpServer)
  }

}

class Api(statsReceiver: StatsReceiver) extends Service[Request, Response] {
  val root: Endpoint[String] = get(/) { Ok("hello") }
  val ping: Endpoint[String] = get("ping") { Ok("pong") }

  override def apply(request: Request) = (root :+: ping).toServiceAs[Text.Plain].apply(request)
}
