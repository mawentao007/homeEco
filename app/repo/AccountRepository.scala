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
    if(accountTableQuery.size.result  != 0) {
      val lastId:Rep[Option[Int]] = accountTableQuery.map(_.id).max
      logger.info(lastId.toString())

      val lastBalance = db.run {
        accountTableQuery.filter(_.id === lastId).map(_.balance).result.head
      }
      val newBalance = Await.result(lastBalance,Duration.Inf) + detail.amount
      val fId = db.run {
        accountTableQueryInc +=
          Detail(detail.date, detail.io, detail.amount, Some(newBalance), detail.reason)
      }
      (newBalance,fId)
    }else{
      val fId = db.run {
        accountTableQueryInc +=
          Detail(detail.date, detail.io, detail.amount, Some(detail.amount), detail.reason)
      }
      (detail.amount,fId)
    }


  }


  def insertAll(details: List[Detail]): Future[Seq[Int]] = db.run {
    accountTableQueryInc ++= details
  }

  /**
    * 只能修改列表结尾的条目，历史账单无法修改
    *
    * @param detail
    * @return
    */
  def update(detail: Detail): (Double,Future[Int]) = {
    db.run {
      accountTableQuery.filter(_.id === detail.id).delete
    }
    insert(detail)
  }

  def delete(id: Int): Future[Int] = db.run {
    accountTableQuery.filter(_.id === id).delete
  }

  def getAll(): Future[List[Detail]] = db.run {
    accountTableQuery.to[List].result
  }

  def getById(empId: Int): Future[Option[Detail]] = db.run {
    accountTableQuery.filter(_.id === empId).result.headOption
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
    def id = column[Int]("id",O.AutoInc,O.PrimaryKey)


    //def emailUnique = index("email_unique_key", email, unique = true)

    def * = (date, io, amount, balance.?,reason, id.?) <>( Detail.tupled,Detail.unapply)
    /**
      * 自定义映射的方法
      * def * = (date, io, amount, balance,reason, id.?).shaped.<>(
      * {t =>Detail.apply(t._1,t._2,(Math.round(t._3 * 100) / 100.0).toFloat,t._4,t._5,t._6)},
      * Detail.unapply)*/
  }

}

