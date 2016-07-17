package repo

import javax.inject.{Inject, Singleton}


import scala.concurrent.duration._
import models.Detail
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{Await, Future}
import play.api.Logger


@Singleton()
class AccountRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends AccountTable with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val logger = Logger(this.getClass())

  def insert(detail: Detail):(Double,Future[Int]) = {
    val fuLastId:Future[Option[Int]] = db.run{
      accountTableQuery.map(_.id).max.result
    }

    val lastId = Await.result(fuLastId,Duration.Inf)
    //如果数据库中不存在其它条目,则当前的amount就是balance
    if(lastId == None) {
      //根据收入或者支出计算balance
      if(detail.io == "收入"){
        val fId = db.run {
          accountTableQueryInc +=
            Detail(detail.date, detail.io, detail.amount, Some(detail.amount), detail.reason,detail.user,Some(1))
        }
        (detail.amount,fId)
      }else{
        val fId = db.run {
          accountTableQueryInc +=
            Detail(detail.date, detail.io, detail.amount, Some(-detail.amount), detail.reason,detail.user,Some(1))
        }
        (-detail.amount,fId)
      }

    }else{
      val fuLastRow = db.run {
        accountTableQuery.filter(_.id === lastId).result.head
      }
      val lastRow = Await.result(fuLastRow,Duration.Inf)

      val  newBalance = detail.io match{
        case "收入" => lastRow.balance.get + detail.amount
        case "支出" => lastRow.balance.get - detail.amount
      }

      val fId = db.run {
        accountTableQueryInc +=
          Detail(detail.date, detail.io, detail.amount, Some(newBalance), detail.reason,detail.user,Some(1))
      }

      //更新之前行
      //更新时候注意提供带有id的完整数据
      db.run{
        accountTableQuery.filter(_.id === lastId).update(
          Detail(lastRow.date,lastRow.io,lastRow.amount,lastRow.balance,lastRow.reason,detail.user,Some(0),lastId)
        )
      }
      (newBalance,fId)
    }


  }


  def insertAll(details: List[Detail]): Future[Seq[Int]] = db.run {
    accountTableQueryInc ++= details
  }

  /**
    * 只能修改列表结尾的条目，历史账单无法修改
    * 更新历史条目的功能废弃,因为余额的处理比较麻烦。可以考虑只允许更新reason等内容。
    */
//  def update(detail: Detail): (Double,Future[Int]) = {
//    db.run {
//      accountTableQuery.filter(_.id === detail.id).delete
//    }
//    insert(detail)
//  }

//TODO 删除条目之后需要更新最后一个条目的whetherLatest字段
  def delete(id: Int): Future[Int] = db.run {
      accountTableQuery.filter(_.id === id).filter(_.whetherLatest === 1).delete
  }

  def getAll(): Future[List[Detail]] = db.run {
    accountTableQuery.to[List].result
  }

  def getById(detId: Int): Future[Option[Detail]] = db.run {
    accountTableQuery.filter(_.id === detId).result.headOption
  }

  def ddl = accountTableQuery.schema

}

private[repo] trait AccountTable {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  lazy protected val accountTableQuery = TableQuery[accountTable]
  lazy protected val accountTableQueryInc = accountTableQuery returning accountTableQuery.map(_.id)

  class accountTable(tag: Tag) extends Table[Detail](tag, "account") {
    def date = column[String]("date")
    def io = column[String]("io")
    def amount = column[Double]("amount")  //double可以避免float引起的精度偏移
    def balance = column[Double]("balance")
    def reason = column[String]("reason")
    def user = column[String]("user")
    def whetherLatest = column[Int]("whetherLatest")
    def id = column[Int]("id",O.AutoInc,O.PrimaryKey)



    //def emailUnique = index("email_unique_key", email, unique = true)

    def * = (date, io, amount, balance.?,reason,user,whetherLatest.?,id.?) <>( Detail.tupled,Detail.unapply)
    /**
      * 自定义映射的方法
      * def * = (date, io, amount, balance,reason, id.?).shaped.<>(
      * {t =>Detail.apply(t._1,t._2,(Math.round(t._3 * 100) / 100.0).toFloat,t._4,t._5,t._6)},
      * Detail.unapply)*/
  }

}

