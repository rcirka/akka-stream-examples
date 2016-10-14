import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import api.TvMazeApi
import com.typesafe.scalalogging.StrictLogging
import mongodb.collections.ShowCollection
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by rcirk_000 on 10/1/2016.
  */
trait Dependencies {
  this: StrictLogging =>

  val decider: Supervision.Decider = {
    ex: Throwable =>
      logger.error("Stream error", ex)
      Supervision.Resume
  }

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system)
    .withSupervisionStrategy(decider))
  implicit val ec = system.dispatcher

  val mongoDriver = new reactivemongo.api.MongoDriver
  val mongoConnection = mongoDriver.connection(List("localhost"))
  val mongoDb = Await.result(mongoConnection.database("akka-streams"), Duration.Inf)
  val showCollection = new ShowCollection(mongoDb)

  val wsClient = NingWSClient()

  val tvApi = new TvMazeApi(wsClient)

  val imagePath = Paths.get("images/")
  if (!Files.exists(imagePath))
    Files.createDirectory(imagePath)



  def shutdown() = {
    mongoConnection.close()
    mongoDriver.close()
    wsClient.close()
    system.shutdown()
  }
}
