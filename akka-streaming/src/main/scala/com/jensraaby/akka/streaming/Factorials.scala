package com.jensraaby.akka.streaming

import akka.stream._
import akka.stream.scaladsl._

import akka.{ NotUsed, Done }
import akka.actor.ActorSystem
import akka.util.ByteString
import scala.concurrent._
import scala.concurrent.duration._
import java.nio.file.Paths

object Factorials extends App {
  implicit val system = ActorSystem("main")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)

  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)

//  val result = factorials.map(_.toString).runWith(lineSink("factorials.txt"))
//  result.onComplete(_ => system.terminate())

  val pretty = factorials.zipWith(Source(0 to 100))((num, idx) => s"$idx! = $num")
    .throttle(1, 1.second, 1, ThrottleMode.shaping)
    .runForeach(println)

  pretty.onComplete(_ => system.terminate())
}
