package models




case class Detail(date: String,
                  io: String,
                  amount: Double,
                  balance:Option[Double]=None,
                  reason:String,
                  whetherLatest:Option[Int] = None ,
                  id: Option[Int]=None          //注意option类型导致的数据丢失
                 )




