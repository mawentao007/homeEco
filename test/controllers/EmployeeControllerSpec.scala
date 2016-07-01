package controllers


import models.Detail
import org.specs2.mock.Mockito
import play.api.Environment
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import repo.AccountRepository
import utils.JsonFormat._

import scala.concurrent.Future

class EmployeeControllerSpec extends PlaySpecification with Mockito with Results {

  implicit val mockedRepo: AccountRepository = mock[AccountRepository]


  "EmployeeController " should {

    "create a employee" in new WithEmpApplication() {
      val emp = Detail("sky", "sky@knoldus.com", "knoldus", "Senior Consultant")
      mockedRepo.insert(emp) returns Future.successful(1)
      val result = employeeController.create().apply(FakeRequest().withBody(Json.toJson(emp)))
      val resultAsString = contentAsString(result)
      resultAsString === """{"status":"success","data":{"id":1},"msg":"Employee has been created successfully."}"""
    }

    "update a employee" in new WithEmpApplication() {
      val updatedEmp = Detail("Satendra", "sky@knoldus.com", "knoldus", "Senior Consultant", Some(1))
      mockedRepo.update(updatedEmp) returns Future.successful(1)

      val result = employeeController.update().apply(FakeRequest().withBody(Json.toJson(updatedEmp)))
      val resultAsString = contentAsString(result)
      resultAsString === """{"status":"success","data":"{}","msg":"Employee has been updated successfully."}"""
    }

    "edit a employee" in new WithEmpApplication() {
      val emp = Detail("sky", "sky@knoldus.com", "knoldus", "Senior Consultant", Some(1))
      mockedRepo.getById(1) returns Future.successful(Some(emp))
      val result = employeeController.edit(1).apply(FakeRequest())
      val resultAsString = contentAsString(result)
      resultAsString === """{"status":"success","data":{"name":"sky","email":"sky@knoldus.com","companyName":"knoldus","position":"Senior Consultant","id":1},"msg":"Getting Employee successfully."}"""
    }

    "delete a employee" in new WithEmpApplication() {
      mockedRepo.delete(1) returns Future.successful(1)
      val result = employeeController.delete(1).apply(FakeRequest())
      val resultAsString = contentAsString(result)
      resultAsString === """{"status":"success","data":"{}","msg":"Employee has been deleted successfully."}"""
    }

    "get all list" in new WithEmpApplication() {
      val emp = Detail("sky", "sky@knoldus.com", "knoldus", "Senior Consultant", Some(1))
      mockedRepo.getAll() returns Future.successful(List(emp))
      val result = employeeController.list().apply(FakeRequest())
      val resultAsString = contentAsString(result)
      resultAsString === """{"status":"success","data":[{"name":"sky","email":"sky@knoldus.com","companyName":"knoldus","position":"Senior Consultant","id":1}],"msg":"Getting Employee list successfully."}"""
    }

  }

}

class WithEmpApplication(implicit mockedRepo: AccountRepository) extends WithApplication {
  val messageAPI = new DefaultMessagesApi(Environment.simple(), app.configuration, new DefaultLangs(app.configuration))
  val employeeController = new AccountController(mockedRepo, messageAPI)
}

