package controllers

import java.text.SimpleDateFormat
import java.util.Calendar

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
    request.body.validate[(Option[String],Option[String])].fold(error => Future.successful(BadRequest(JsError.toJson(error))),{
      case (bdate,edate) =>
        //java.time.LocalDate.now
//        val format = new SimpleDateFormat("yyyy-mm-dd")
//        format.format(Calendar.getInstance().getTime())
        val beginDate = bdate.getOrElse("2016-07-01")
        val endDate = edate.getOrElse(java.time.LocalDate.now.toString)

        logger.info("begindate " + endDate.toString)
        accRepository.querySql(beginDate,endDate) map { details:List[Detail] =>
          //income part
          val allIncome = details.filter(_.io == "收入")
          val incomeAmount = allIncome.map(_.amount).foldRight(0.0)(_ + _)

          //expense part
          val allExpense = details.filter(_.io == "支出")
          val expenseAmount = allExpense.map(_.amount).foldRight(0.0)(_ + _)

          val netIncome = incomeAmount - expenseAmount


          //用户区间总支出
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

          //用户区间总收入
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

          //按种类区分支出
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


          //支出时间图
          val xAxisJson = Json.toJson(details.filter(_.io == "支出").map(_.date))
          val yDataJson = Json.toJson(
            details.filter(_.io == "支出").map(_.amount)
          )

          //按月统计收入
          val incomeByMonth = getDetailsByMonth(details).map{
            case (month:String,dets) =>
              (month,dets.filter(_.io == "收入").map(_.amount).foldRight(0.0)(_ + _))
          }

          val incomeByMonthJson = Json.obj(
            "time" -> Json.toJson(incomeByMonth.map(_._1)),
            "amount" -> Json.toJson(incomeByMonth.map(_._2))
          )

          //按月统计支出
          val expenseByMonth = getDetailsByMonth(details).map {
            case (month: String, dets: List[Detail]) =>
              (month, dets.filter(_.io == "支出").map(_.amount).foldRight(0.0)(_ + _))
          }
          val expenseByMonthJson = Json.obj(
            "time" -> Json.toJson(expenseByMonth.map(_._1)),
            "amount" -> Json.toJson(expenseByMonth.map(_._2))
          )

          //按月统计净收入
          val netIncomeByMonthJson = Json.obj(
            "time" -> Json.toJson(expenseByMonth.map(_._1)),
            "amount" -> Json.toJson((incomeByMonth.map(_._2),expenseByMonth.map(_._2)).zipped.map(_ - _))
          )
          logger.info(netIncomeByMonthJson.toString())



          Ok(successResponse(
            JsObject(
              Seq(
                "incomeJson"->incomeJson,
                "expenseJson"-> expenseJson,
                "detail" -> Json.toJson(details),
                "kindJson" -> kindExpenseJson,
                "xAxisJson" -> xAxisJson,
                "yDataJson" -> yDataJson,
                "incomeByMonth" -> incomeByMonthJson,
                "expenseByMonth" -> expenseByMonthJson,
                "netIncomeByMonth" -> netIncomeByMonthJson
              )

            ),
              Messages("detail.success.detailList")))
        }
    })
  }

  def getDetailsByMonth(details:List[Detail]):List[(String,List[Detail])] = {
    val groupByYear = details.groupBy(_.date.split('-').take(2).mkString("-")).map{
      case (mon,det) =>
        (mon,det)
    }
    groupByYear.toList
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
        (JsPath \ "beginDate").readNullable[String] and
        (__ \ 'endDate).readNullable[String]
//        (__ \'user).read[String] and
//        (__ \'io).read[String]
    ) tupled

}



