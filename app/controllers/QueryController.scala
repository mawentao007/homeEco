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

import scala.collection.mutable
import scala.concurrent.Future

/**
  * Handles all requests related to account
  */
class QueryController @Inject()(accRepository: AccountRepository, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  import Constants._

  val logger = Logger(this.getClass())


  /**
    * Handles request for getting all account from the database
    */
  def query() = Action.async(parse.json) { request =>
    request.body.validate[(String,String)].fold(error => Future.successful(BadRequest(JsError.toJson(error))),{
      case (bdate,edate) =>
        accRepository.querySql(bdate,edate) map { details =>
          //income part
          val allIncome = details.filter(_.io == "收入")
          //val suIncome = allIncome.filter( _.user == "粟样丹").map(_.amount).foldRight(0.0)(_ + _)
          //val maIncome = allIncome.filter( _.user == "Marvin").map(_.amount).foldRight(0.0)(_ + _)
          val incomeAmount = allIncome.map(_.amount).foldRight(0.0)(_ + _)

          //expense part
          val allExpense = details.filter(_.io == "支出")
          //val suExpense = allExpense.filter( _.user == "粟样丹").map(_.amount).foldRight(0.0)(_ + _)
          //val maExpense = allExpense.filter( _.user == "Marvin").map(_.amount).foldRight(0.0)(_ + _)
          val expenseAmount = allExpense.map(_.amount).foldRight(0.0)(_ + _)

          //val suNetIncome = suIncome - suExpense
          //val maNetIncome = maIncome - maExpense
          val netIncome = incomeAmount - expenseAmount


          //logger.info("su income " + suIncome + " ma income " + maIncome + " su Expense " + suExpense + " ma expense " + maExpense + " netincome " + netIncome + " ma net income " +
          //  maNetIncome + " su net income " + suNetIncome)

          val expenses:mutable.HashMap[String,Double] = mutable.HashMap()
          allExpense.groupBy(_.user).foreach{
            case(user,rows) =>
              expenses.put(user,rows.map(_.amount).foldRight(0.0)(_ + _))
          }

          val expenseJson = Json.toJson(
            expenses.map{
              case(k,v) => Json.obj("name" -> k,"y" -> v)
            }
          )

          val income:mutable.HashMap[String,Double] = mutable.HashMap()
          allIncome.groupBy(_.user).foreach{
            case(user,rows) =>
              income.put(user,rows.map(_.amount).foldRight(0.0)(_ + _))
          }

          val incomeJson = Json.toJson(
            income.map{
              case(k,v) => Json.obj("name" -> k,"y" -> v)
            }
          )

//          val incomeJson = Json.arr(
//            Json.obj("name"-> "粟样丹收入","y" -> suIncome),
//            Json.obj("name"-> "马文韬收入","y" -> maIncome)
//          )
//
//          val expenseJson = Json.arr(
//            Json.obj("name"-> "粟样丹支出","y" -> suExpense),
//            Json.obj("name"-> "马文韬支出","y" -> maExpense)
//          )


          val kindExpenses:mutable.HashMap[String,Double] = mutable.HashMap()
          allExpense.groupBy(_.kind).foreach{
            case(kind,rows) =>
              kindExpenses.put(kind,rows.map(_.amount).foldRight(0.0)(_ + _))
          }

          val kindExpenseJson = Json.toJson(
            kindExpenses.map{
              case(k,v) => Json.obj("name" -> k,"y" -> v)
            }
          )


          Ok(successResponse(
            JsObject(
              Seq(
                "incomeJson"->incomeJson,
                "expenseJson"-> expenseJson,
                "detail" -> Json.toJson(details),
                "kindJson" -> kindExpenseJson
              )

            ),
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
        (__ \ 'endDate).read[String]
//        (__ \'user).read[String] and
//        (__ \'io).read[String]
    ) tupled

}



