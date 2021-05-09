package fixture

import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import scala.reflect.ClassTag
import slick.jdbc.JdbcProfile
import weather.{MockWeatherService, WeatherService}

trait BaseSpec
  extends PlaySpec
    with GuiceOneAppPerSuite
    with Results
    with BeforeAndAfterAll
    with FutureAwaits
    with DefaultAwaitTimeout {

  override def fakeApplication(): Application = {
    GuiceApplicationBuilder()
      .overrides(bind[WeatherService].to[MockWeatherService])
      .build()
  }

  protected def instanceOf[T: ClassTag]: T = app.injector.instanceOf[T]

  val dbConfig = instanceOf[DatabaseConfigProvider].get[JdbcProfile]
  val database = instanceOf[DBApi].database("default")
  import dbConfig.profile.api._

  protected def execute[T](query: Query[_, T, Seq]): Seq[T] = {
    await(dbConfig.db.run(query.result))
  }

  override def beforeAll(): Unit = {
    Evolutions.cleanupEvolutions(database)
    Evolutions.applyEvolutions(database)
    instanceOf[UsersFixture].insertUsers()
    instanceOf[RecordsFixture].insertRecords()
  }
}
