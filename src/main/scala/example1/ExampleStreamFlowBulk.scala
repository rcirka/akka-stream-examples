package example1

import _root_.Dependencies
import akka.stream.scaladsl.{Sink, Source}
import com.typesafe.scalalogging.StrictLogging


object ExampleStreamFlowBulk extends App with Dependencies with StrictLogging {

  val source = Source(List("vampire", "space", "reality"))
  val sink = Sink.ignore

  source
  .mapAsyncUnordered(8)(tvApi.search)
  .mapConcat(_.map(_.show).toList)
  .grouped(10)
  .mapAsyncUnordered(1)(showCollection.insertBulk)
  .runWith(sink)
  .onComplete { _ =>
    logger.info("Done!")
    shutdown()
  }

}


