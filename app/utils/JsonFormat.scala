package utils



import models._
import play.api.libs.json.{Writes, Json}


object JsonFormat {

  implicit val detailFormat = Json.format[Detail]
/*  implicit def writes(detail: Detail) = Json.obj(
    "date" -> detail.date,
    "io" -> detail.io,
    "amount" -> (detail.amount - detail.amount % 0.01),
    "balance" -> (detail.balance - detail.balance % 0.01),
    "reason" -> detail.reason,
    "id" -> detail.id.get
  )*/

}


