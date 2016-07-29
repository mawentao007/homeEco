package models




case class Detail(date: String,
                  io: String,
                  kind:String,
                  amount: Double,
                  balance:Option[Double]=None,
                  reason:String,
                  user:String,
                  whetherLatest:Option[Int] = None ,
                  id: Option[Int]=None          //注意option类型导致的数据丢失
                 )

/**
  * 1.Model添加用户字段;
  * 2.更改数据库映射;
  * 3.更新Controller中字段映射;
  * 4.Sql脚本修改，创建新模式数据库;
  * 5.修改html模板;
  * 6.修改js代码。
  */




