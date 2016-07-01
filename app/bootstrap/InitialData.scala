package bootstrap

import com.google.inject.Inject
import javax.inject.Singleton
import repo.AccountRepository
import models.Detail
import java.util.Date
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.Logger
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class InitialData @Inject() (accountRepo: AccountRepository) {

  def insert = for {
    details <- accountRepo.getAll() if (details.length == 0)
    _ <- accountRepo.insertAll(Data.details)
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
  val details = List(
    //Detail("2", 2, 2,2,"2")
  )
}
