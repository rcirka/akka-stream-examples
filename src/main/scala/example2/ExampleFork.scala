package example2

import _root_.Dependencies
import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import models.SearchResult
import mongodb.collections.ShowCollection


object ExampleFork extends App with Dependencies with StrictLogging {

  val highShowCollection = new ShowCollection(mongoDb, "HighShows")
  val lowShowCollection = new ShowCollection(mongoDb, "LowShows")

  val source = Source(List("vampire", "space", "reality"))
  val sink = Sink.ignore

  val graph = GraphDSL.create() { implicit builder =>
    val searchResults = builder.add(
      Flow[String]
      .mapAsyncUnordered(8)(x => tvApi.search(x))
      .mapConcat(_.toList)
    )

    val B = builder.add(Broadcast[SearchResult](2))

    val isHighlyRated = builder.add(Flow[SearchResult].filter(_.score >= 7))
    val isLowlyRated = builder.add(Flow[SearchResult].filter(_.score < 7))
    val insertIntoMongoHigh = builder.add(Flow[SearchResult].mapAsyncUnordered(8)(searchResult => highShowCollection.upsert(searchResult.show).map(_ => ())))
    val insertIntoMongoLow = builder.add(Flow[SearchResult].mapAsyncUnordered(8)(searchResult => lowShowCollection.upsert(searchResult.show).map(_ => ())))
    val done = builder.add(Merge[Unit](2))


    searchResults ~> B ~> isHighlyRated ~> insertIntoMongoHigh ~> done
                     B ~> isLowlyRated  ~> insertIntoMongoLow  ~> done

    FlowShape(searchResults.in, done.out)
  }

  source
  .via(graph)
  .runWith(sink)
  .onComplete {_ =>
    logger.info("Done!")
    shutdown()
  }
}
