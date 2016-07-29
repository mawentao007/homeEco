package repo

import javax.inject.{Inject, Singleton}

import scala.concurrent.duration._
import models.Detail
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{Await, Future}
import play.api.Logger
import slick.lifted.CanBeQueryCondition
import slick.model.Column


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
            Detail(detail.date, detail.io, detail.kind, detail.amount, Some(detail.amount), detail.reason,detail.user,Some(1))
        }
        (detail.amount,fId)
      }else{
        val fId = db.run {
          accountTableQueryInc +=
            Detail(detail.date, detail.io, detail.kind, detail.amount, Some(-detail.amount), detail.reason,detail.user,Some(1))
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
          Detail(detail.date, detail.io, detail.kind, detail.amount, Some(newBalance), detail.reason,detail.user,Some(1))
      }

      //更新之前行
      //更新时候注意提供带有id的完整数据
      db.run{
        accountTableQuery.filter(_.id === lastId).update(
          Detail(lastRow.date,lastRow.io, lastRow.kind, lastRow.amount,lastRow.balance,lastRow.reason,detail.user,Some(0),lastId)
        )
      }
      (newBalance,fId)
    }


  }


  def insertAll(details: List[Detail]): Future[Seq[Int]] = db.run {
    accountTableQueryInc ++= details
  }


  /**
    * 根据起始和终止时间筛选条目,包含起始和终止时间
    * @param beginDate
    * @param endDate
    * @return
    */
  def querySql(beginDate:String,endDate:String) ={
    var query = accountTableQuery.filter(x =>(x.date >= beginDate && x.date <= endDate))
//    if(user != "所有"){
//      query = query.filter(_.user === user)
//    }
   /* if(io != "所有类型"){
      query = query.filter(_.io === io)
    }*/

    db.run{
      query.to[List].result
    }
  }






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
    def kind = column[String]("kind")
    def amount = column[Double]("amount")  //double可以避免float引起的精度偏移
    def balance = column[Double]("balance")
    def reason = column[String]("reason")
    def user = column[String]("user")
    def whetherLatest = column[Int]("whetherLatest")
    def id = column[Int]("id",O.AutoInc,O.PrimaryKey)



    //def emailUnique = index("email_unique_key", email, unique = true)

    def * = (date, io, kind, amount, balance.?,reason,user,whetherLatest.?,id.?) <>( Detail.tupled,Detail.unapply)
    /**
      * 自定义映射的方法
      * def * = (date, io, amount, balance,reason, id.?).shaped.<>(
      * {t =>Detail.apply(t._1,t._2,(Math.round(t._3 * 100) / 100.0).toFloat,t._4,t._5,t._6)},
      * Detail.unapply)*/
  }

}

