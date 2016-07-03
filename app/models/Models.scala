package models


import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Json, Reads, JsPath}

case class Detail(date: String,
                  io: String,
                  amount: Float,
                  balance:Float,
                  reason:String,
                  id: Option[Int]=None
                    )




