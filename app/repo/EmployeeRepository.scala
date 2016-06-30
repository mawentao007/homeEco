package repo

import javax.inject.{Inject, Singleton}

import models.Employee
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

@Singleton()
class EmployeeRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends EmployeeTable with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  def insert(employee: Employee): Future[Int] = db.run {
    empTableQueryInc += employee
  }

  def insertAll(employees: List[Employee]): Future[Seq[Int]] = db.run {
    empTableQueryInc ++= employees
  }

  def update(employee: Employee): Future[Int] = db.run {
    empTableQuery.filter(_.id === employee.id).update(employee)
  }

  def delete(id: Int): Future[Int] = db.run {
    empTableQuery.filter(_.id === id).delete
  }

  def getAll(): Future[List[Employee]] = db.run {
    empTableQuery.to[List].result
  }

  def getById(empId: Int): Future[Option[Employee]] = db.run {
    empTableQuery.filter(_.id === empId).result.headOption
  }

  def ddl = empTableQuery.schema

}

private[repo] trait EmployeeTable {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import driver.api._

  lazy protected val empTableQuery = TableQuery[EmployeeTable]
  lazy protected val empTableQueryInc = empTableQuery returning empTableQuery.map(_.id)

  class EmployeeTable(tag: Tag) extends Table[Employee](tag, "employee") {
    def id = column[Int]("id",O.AutoInc,O.PrimaryKey)
    def name = column[String]("name", O.SqlType("VARCHAR(200)"))
    def email = column[String]("email", O.SqlType("VARCHAR(200)"))
    def companyName = column[String]("company_name")
    def position = column[String]("position")
    def time = column[String]("time")

    def emailUnique = index("email_unique_key", email, unique = true)

    def * = (name, email, companyName, position,time, id.?) <>(Employee.tupled, Employee.unapply)
  }

}

