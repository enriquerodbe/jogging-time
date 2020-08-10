package scheduler

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import javax.inject.{Inject, Named, Singleton}
import report.AverageReportsActor.CreateAverageReports

@Singleton
class Scheduler @Inject()(
    system: ActorSystem,
    @Named("averageReportsActor") averageReportsActor: ActorRef) {

  val scheduler = QuartzSchedulerExtension(system)

  scheduler.schedule(
    name = "CreateAverageReports",
    receiver = averageReportsActor,
    msg = CreateAverageReports,
  )
}
