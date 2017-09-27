package com.jensraaby.ddia.graphql
import io.circe.Json
import sangria.execution._
import sangria.marshalling.circe._

import sangria.macros._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

object GraphQLTest extends App {

  val query =
    graphql"""
             query MyProduct {
               product(id: "1") {
                name
                description

                picture(size: 500) {
                  width, height, url
                }
               }

              products {
                            name
                          }
             }"""


  val result: Future[Json] = Executor.execute(graphql.schema, query, new ProductRepo)

  println(Await.result(result, Duration.Inf))
}
