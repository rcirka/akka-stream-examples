package api

import akka.stream.Materializer
import models.{SearchResult, Show}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by rcirk_000 on 9/29/2016.
  */
class TvMazeApi(wsClient: WSClient)(implicit m: Materializer, ec: ExecutionContext) {
  val baseUrl = "http://api.tvmaze.com/"

  implicit val showFormat = Json.format[Show]
  implicit val searchResultFormat = Json.format[SearchResult]

  protected def get(path: String) = {
    val url = baseUrl + path

    wsClient.url(url).get.map{_.json}

  }

  def search(query: String): Future[Seq[SearchResult]] = {
    get(s"search/shows?q=$query").map(_.as[Seq[SearchResult]])
  }
}
