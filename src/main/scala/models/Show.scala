package models

/**
  * Created by rcirk_000 on 9/29/2016.
  */
case class Show(
  id: Int,
  name: String,
  `type`: String,
  image: Map[String, String],
//  rating: Map[String, Option[Double]],
  genres: Seq[String]
)
