package example1

import _root_.Dependencies
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object ExampleMap extends App with Dependencies with StrictLogging {

  val searchTerms = List("vampire", "space", "reality")
  val searchFutures = searchTerms.map(tvApi.search)

  val searchF = Future.sequence(searchFutures)
    .map { results =>
      results.flatten
    }
    .flatMap { results =>
      val futures = results.map { result =>
        showCollection.upsert(result.show)
      }

      Future.sequence(futures)
    }

  val result = Await.result(searchF, Duration.Inf)

  println(result)

  shutdown()
}
