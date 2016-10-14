package mongodb.collections

import models.Show
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Created by rcirk_000 on 10/1/2016.
  */
class ShowCollection(db: DefaultDB, name: String = "show")(implicit ec: ExecutionContext) {
  def collection = db.collection[BSONCollection](name)

  implicit def MapWriter[V](implicit vw: BSONDocumentWriter[V]): BSONDocumentWriter[Map[String, V]] = new BSONDocumentWriter[Map[String, V]] {
    def write(map: Map[String, V]): BSONDocument = {
      val elements = map.toStream.map { tuple =>
        tuple._1 -> vw.write(tuple._2)
      }
      BSONDocument(elements)
    }
  }

  implicit object BSONMapHandler extends BSONHandler[BSONDocument, Map[String, String]] {
    override def read(bson: BSONDocument): Map[String, String] = {
      bson.elements.map {
        case (key, value) => key -> value.asInstanceOf[BSONString].value
      }.toMap
    }

    override def write(t: Map[String, String]): BSONDocument = {
      val stream: Stream[Try[(String, BSONString)]] = t.map {
        case (key, value) => Try((key, BSONString(value)))
      }.toStream
      BSONDocument(stream)
    }
  }

  implicit val showHandler = Macros.writer[Show]

  def upsert(show: Show): Future[UpdateWriteResult] = {
    val query = BSONDocument("id" -> show.id)
    collection.update(query, show, upsert = true)
  }

  def insertBulk(shows: Seq[Show]): Future[Unit] = {
    // To do: Implement
    Future(())
  }
}
