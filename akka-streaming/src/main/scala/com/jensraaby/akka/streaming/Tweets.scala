package com.jensraaby.akka.streaming

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy}
import akka.stream.scaladsl._

import scala.concurrent.Future

final case class Author(handle: String)

final case class Hashtag(name: String)

final case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] = body.split(" ").collect {
    case t if t.startsWith("#") => Hashtag(t.replaceAll("[^#\\w]", ""))
  }.toSet
}

object Tweets extends App {
  val akkaTag = Hashtag("#akka")

  val tweets: Source[Tweet, NotUsed] = Source(
    Tweet(Author("rolandkuhn"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!") ::
      Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !") ::
      Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!") ::
      Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!") ::
      Tweet(Author("drama"), System.currentTimeMillis, "we compared #apples to #oranges!") ::
      Nil)

  implicit val system = ActorSystem("reactive-tweets")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val authors: Source[Author, NotUsed] = tweets.filter(_.hashtags.contains(akkaTag)).map(_.author)
  val hashtags: Source[Hashtag, NotUsed] = tweets.mapConcat(_.hashtags.toList)

//  val s = tweets
//    .map(_.hashtags) // Get all sets of hashtags ...
//    .reduce(_ ++ _) // ... and reduce them to a single set, removing duplicates across all tweets
//    .mapConcat(identity) // Flatten the stream of tweets to a stream of hashtags
//    .map(_.name.toUpperCase) // Convert all hashtags to upper case
//    .runWith(Sink.foreach(println)) // Attach the Flow to a Sink that will finally print the hashtags


//  val s2 = authors.runWith(Sink.foreach(println))
//  s2.onComplete(_ => system.terminate())

//  val writeAuthors: Sink[Author, NotUsed] = Sink.foreach((author: Author) => println(author))
//  val writeHashtags: Sink[Hashtag, NotUsed] = Sink.foreach(println)
//  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
//    import GraphDSL.Implicits._
//
//    val bcast = b.add(Broadcast[Tweet](2))
//    tweets ~> bcast.in
//    bcast.out(0) ~> Flow[Tweet].map(_.author) ~> writeAuthors
//    bcast.out(1) ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashtags
//    ClosedShape
//  })
//  g.run()

  val count: Flow[Tweet, Int, NotUsed] = Flow[Tweet].map(_ => 1)
  val sumSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)
  val counterGraph: RunnableGraph[Future[Int]] =
    tweets
      .via(count)
      .toMat(sumSink)(Keep.right)

  val sum: Future[Int] = counterGraph.run()
  sum.foreach(c => println(s"Total tweets processed: $c"))

}
