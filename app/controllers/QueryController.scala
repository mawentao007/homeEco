package controllers

import com.google.inject.Inject
import models.Detail
import play.api.Logger
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json._
import play.api.libs.json.{JsObject, _}
import play.api.mvc._
import repo.AccountRepository
import utils.Constants
import utils.JsonFormat._
import views.html
import play.api.libs.functional.syntax._

import scala.concurrent.Future

/**
  * Handles all requests related to account
  */
class QueryController @Inject()(accRepository: AccountRepository, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  import Constants._

  val logger = Logger(this.getClass())

  def queryIndex() = Action{
    Ok(html.queryIndex())
  }

  /**
    * Handles request for getting all account from the database
    */
  def query() = Action.async(parse.json) { request =>
    request.body.validate[(String,String,String,String)].fold(error => Future.successful(BadRequest(JsError.toJson(error))),{
      case (bdate,edate,user,io) =>
        accRepository.querySql(bdate,edate,user,io) map { details =>
          Ok(successResponse(
            JsObject(Seq("test"->JsNumber(10),"detail" -> Json.toJson(details))),
              Messages("detail.success.detailList")))
        }
    })
  }

  //JsObject(Seq("key" -> JsObject))
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
      //Redirect("/detail/list")
    })
  }

  /**
    * Handles request for deletion of existing account by account_id
    *
    *
    */
  //TODO   删除失败的情况要进行提示。
  def delete(accId: Int) = Action.async { request =>
    accRepository.delete(accId).map { x =>
      if(x == 1) {
        Ok(successResponse(Json.toJson("{}"), Messages("detail.success.deleted")))
      }else{
        Ok(successResponse(Json.toJson("{}"), Messages("detail.error.deleted")))
      }
    }
  }

  /**
    * Handles request for get account details for editing
    */
  def edit(accId: Int): Action[AnyContent] = Action.async { request =>
    accRepository.getById(accId).map { accOpt =>
      accOpt.fold(Ok(errorResponse(Json.toJson("{}"), Messages("detail.error.detailNotExist"))))(acc => Ok(
        successResponse(Json.toJson(acc), Messages("detail.success.detail"))))
    }
  }

  private def errorResponse(data: JsValue, message: String) = {
    obj("status" -> ERROR, "data" -> data, "msg" -> message)
  }



  private def successResponse(data: JsValue, message: String) = {
    obj("status" -> SUCCESS, "data" -> data, "msg" -> message)
  }

  implicit val queryFormJson = (
    //readNullable 读取Option类型
    //    (JsPath \ "beginDate").readNullable[String] and
        (JsPath \ "beginDate").read[String] and
        (__ \ 'endDate).read[String] and
        (__ \'user).read[String] and
        (__ \'io).read[String]
    ) tupled

}



