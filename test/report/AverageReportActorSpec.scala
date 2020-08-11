package report

import akka.actor.ActorRef
import fixture.BaseSpec
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import play.api.inject.BindingKey
import report.AverageReportsActor.CreateAverageReports

class AverageReportActorSpec
  extends BaseSpec with Eventually with IntegrationPatience {

  val actor = app.injector.instanceOf(
    BindingKey(classOf[ActorRef]).qualifiedWith("averageReportsActor"))
  val averageReportDao = instanceOf[AverageReportDao]

  "AverageReportActor" should {
    "trigger report creation" in {
      execute(averageReportDao.averageReports) must have size 0
      actor ! CreateAverageReports
      eventually {
        execute(averageReportDao.averageReports) must have size 3
      }
    }
  }
}
