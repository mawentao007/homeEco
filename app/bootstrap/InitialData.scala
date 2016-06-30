package bootstrap

import com.google.inject.Inject
import javax.inject.Singleton
import repo.EmployeeRepository
import models.Employee
import java.util.Date
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.Logger
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class InitialData @Inject() (employeeRepo: EmployeeRepository) {

  def insert = for {
    emps <- employeeRepo.getAll() if (emps.length == 0)
    _ <- employeeRepo.insertAll(Data.employees)
  } yield {}

  try {
    Logger.info("DB initialization.................")
    Await.result(insert, Duration.Inf)
  } catch {
    case ex: Exception =>
      Logger.error("Error in database initialization ", ex)
  }

}

object Data {
  val employees = List(
    Employee("Satendra", "satendra@knoldus.com", "Knoldus","Senior Consultant","2001-10-06"),
    Employee("Mayank", "mayank@knoldus.com",  "knoldus","Senior Consultant","2001-10-06"),
    Employee("Sushil", "sushil@knoldus.com",  "knoldus","Consultant","2001-10-06"),
    Employee("Narayan", "narayan@knoldus.com",  "knoldus","Consultant","2001-10-06"),
    Employee("Himanshu", "himanshu@knoldus.com",  "knoldus","Senior Consultant","2001-10-06"))
}
