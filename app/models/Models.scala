package models

import org.joda.time.DateTime

case class Employee(name: String,
                    email: String,
                    companyName: String,
                    position:String,
                    time:String,
                    id: Option[Int]=None
                    )

