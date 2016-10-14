package example1

import _root_.Dependencies
import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import models.{SearchResult, Show}

/**
  * Created by rcirk_000 on 10/1/2016.
  */
object ExampleStreamGraph extends App with Dependencies with StrictLogging {

  val source = Source(List("vampire", "space", "reality"))
  val sink = Sink.ignore

  val graph = GraphDSL.create() { implicit builder =>
    val searchTerm = builder.add(Flow[String])
    val getSearchResults = builder.add(Flow[String].mapAsyncUnordered(8)(tvApi.search))
    val ungroupResults = builder.add(Flow[Seq[SearchResult]].mapConcat(_.map(_.show).toList))
    val insertIntoMongo = builder.add(Flow[Show].mapAsyncUnordered(8)(showCollection.upsert(_).map(_ => ())))

    searchTerm ~> getSearchResults ~> ungroupResults ~> insertIntoMongo

    FlowShape(searchTerm.in, insertIntoMongo.out)
  }

  source
  .via(graph)
  .runWith(sink)
  .onComplete {_ =>
    logger.info("Done!")
    shutdown()
  }
}


