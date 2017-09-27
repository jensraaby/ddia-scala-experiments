package com.jensraaby.rest

import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.stats.{Stat, StatsReceiver}
import com.twitter.finagle.{Http, Service}
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Duration}
import io.finch.Text.Plain
import io.finch._

object Server extends TwitterServer {

  val api: Service[Request, Response] = new Api(statsReceiver)

  val port = flag("http.port", ":8081", "Address port to bind to, prefixed by :")

  override protected val failfastOnFlagsNotParsed = true

  def main(): Unit = {
    val server = Http.server
      .withLabel("Rest-server")
      .withStatsReceiver(statsReceiver)
      .withTransport.verbose
      .withAdmissionControl.concurrencyLimit(maxConcurrentRequests = 1000, maxWaiters = 10)
      .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
      .withRequestTimeout(Duration.fromMilliseconds(500))
      .serve(port(), api)

    onExit {
      server.close()
    }

    Await.ready(adminHttpServer)
  }

}

class Api(statsReceiver: StatsReceiver) extends Service[Request, Response] {
  // Metrics to track
  val pingCounter = statsReceiver.counter("ping")
  val latency: Stat = statsReceiver.stat("latency")

  val root: Endpoint[String] = get(/) {
    Stat.time(latency) { Ok("hello") }
  }

  val ping: Endpoint[String] = get("ping") {
    pingCounter.incr()
    Ok("pong")
  }

  val service = (root :+: ping).toServiceAs[Plain]

  override def apply(request: Request) = service.apply(request)
}
