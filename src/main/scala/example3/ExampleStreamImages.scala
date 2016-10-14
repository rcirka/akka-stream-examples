package example3

import _root_.{Dependencies, FileUtil}
import akka.stream.FlowShape
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import models.Show

/**
  * Created by rcirk_000 on 10/1/2016.
  */
object ExampleStreamImages extends App with Dependencies with StrictLogging {

  val source = Source(List("vampire", "space", "reality"))
  val sink = Sink.ignore

  val graph = GraphDSL.create() { implicit builder =>
    val input = builder.add(
      Flow[String]
      .mapAsyncUnordered(8)(tvApi.search)
      .mapConcat(_.map(_.show).toList)
    )

    val b = builder.add(Broadcast[Show](2))

    val insertIntoMongo = builder.add(Flow[Show].mapAsyncUnordered(8)(showCollection.upsert(_).map(_ => ())))

    val filterAndSaveImages = builder.add(
      Flow[Show]
      .mapConcat(show => FileUtil.extractImageUrls(imagePath, show.image))
      .filterNot{ case (url, path) => FileUtil.imageExists(path)}
      .mapAsyncUnordered(8){ case (url, path) => wsClient.url(url.toString).get().map(response => path -> response.bodyAsBytes)}
      .map{case (path, image) => FileUtil.saveImage(path, image)}
    )

    val done = builder.add(Merge[Unit](2))

    input ~> b ~> insertIntoMongo     ~> done
             b ~> filterAndSaveImages ~> done

    FlowShape(input.in, done.out)
  }

  source
  .via(graph)
  .runWith(sink)
  .onComplete {_ =>
    logger.info("Done!")
    shutdown()
  }


}


