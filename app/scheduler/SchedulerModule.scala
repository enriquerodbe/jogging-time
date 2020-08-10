package scheduler

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import report.AverageReportsActor

class SchedulerModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[AverageReportsActor]("averageReportsActor")
    bind(classOf[Scheduler]).asEagerSingleton()
  }
}
