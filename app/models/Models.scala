package models


import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, JsPath}

case class Detail(date: String,
                  io: Int,
                  amount: Int,
                  balance:Int,
                  reason:String,
                  id: Option[Int]=None
                    )

/*object Detail {
  implicit val reads: Reads[Detail] = (
    (JsPath \ "date").read[String] and
      (JsPath \ "io").read[Int] and
      (JsPath \ "amount").read[Int] and
      (JsPath \ "balance").read[Int] and
      (JsPath \ "reason").read[String]
    )(Detail.apply)

}*/
