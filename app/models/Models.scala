package models




case class Detail(date: String,
                  io: String,
                  amount: Double,
                  balance:Option[Double]=None,
                  reason:String,
                  id: Option[Int]=None
                    )




