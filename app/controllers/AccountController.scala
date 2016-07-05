package controllers

import com.google.inject.Inject
import models.Detail
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.mvc._
import repo.AccountRepository
import utils.Constants
import utils.JsonFormat._

import scala.concurrent.Future

/**
  * Handles all requests related to account
  */
class AccountController @Inject()(accRepository: AccountRepository, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  import Constants._

  val logger = Logger(this.getClass())

  /**
    * Handles request for getting all account from the database
    */
  def list() = Action.async {
    accRepository.getAll().map { res =>
      //logger.info("acc json list: " + Json.toJson(res))
      Ok(successResponse(Json.toJson(res), Messages("acc.success.accList")))
    }
  }

  /**
    * Handles request for creation of new account
    */

  def create() = Action.async(parse.json) { request =>
    request.body.validate[Detail].fold(error => Future.successful(BadRequest(JsError.toJson(error))), { acc =>
      logger.info(acc.toString)
      val balanceAndId = accRepository.insert(acc)
      balanceAndId._2 map { id =>
        Ok(successResponse(
          JsObject(Seq("balance" -> JsNumber(balanceAndId._1),"id" -> JsNumber(id))),
            Messages("成功创建条目")))
      }
    })
  }

  /**
    * Handles request for deletion of existing account by account_id
    */
  def delete(accId: Int) = Action.async { request =>
    accRepository.delete(accId).map { _ =>
      Ok(successResponse(Json.toJson("{}"), Messages("acc.success.deleted")))
    }
  }

  /**
    * Handles request for get account details for editing
    */
  def edit(accId: Int): Action[AnyContent] = Action.async { request =>
    accRepository.getById(accId).map { accOpt =>
      accOpt.fold(Ok(errorResponse(Json.toJson("{}"), Messages("acc.error.accNotExist"))))(acc => Ok(
        successResponse(Json.toJson(acc), Messages("acc.success.account"))))
    }
  }

  private def errorResponse(data: JsValue, message: String) = {
    obj("status" -> ERROR, "data" -> data, "msg" -> message)
  }

  /**
    * Handles request for update existing account
    */
  def update = Action.async(parse.json) { request =>
    request.body.validate[Detail].fold(error => Future.successful(BadRequest(JsError.toJson(error))), { acc =>
      accRepository.update(acc).map { res => Ok(successResponse(Json.toJson("{}"), Messages("acc.success.updated"))) }
    })
  }

  private def successResponse(data: JsValue, message: String) = {
    obj("status" -> SUCCESS, "data" -> data, "msg" -> message)
  }

}



