package repo

import javax.inject.{Inject, Singleton}

import models.Detail
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future
import play.api.Logger


@Singleton()
class AccountRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends AccountTable with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val logger = Logger(this.getClass())

  def insert(detail: Detail): Future[Int] = db.run {
    logger.info(detail.toString)
    accountTableQueryInc += detail
  }

  def insertAll(details: List[Detail]): Future[Seq[Int]] = db.run {
    accountTableQueryInc ++= details
  }

  def update(detail: Detail): Future[Int] = db.run {
    accountTableQuery.filter(_.id === detail.id).update(detail)
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
    def io = column[Int]("io")
    def amount = column[Int]("amount")
    def balance = column[Int]("balance")
    def reason = column[String]("reason")
    def id = column[Int]("id",O.AutoInc,O.PrimaryKey)


    //def emailUnique = index("email_unique_key", email, unique = true)

    def * = (date, io, amount, balance,reason, id.?) <>(Detail.tupled, Detail.unapply)
  }

}

