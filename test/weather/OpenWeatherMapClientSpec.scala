package weather

import com.typesafe.config.Config
import domain.WeatherConditions
import fixture.{BaseSpec, Fixture}
import java.time.Instant
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import play.api.routing.sird._
import play.api.test.WsTestClient
import play.core.server.Server
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class OpenWeatherMapClientSpec extends BaseSpec {

  override def fakeApplication(): Application = {
    GuiceApplicationBuilder()
      .configure("openWeatherMap.baseUrl" -> "")
      .build()
  }
  val config = instanceOf[Config]

  "OpenWeatherMapClient" should {
    "retrieve weather" in {
      Server.withRouterFromComponents() { components =>
        import components.{defaultActionBuilder => Action}
        {
          case GET(p"/data/2.5/onecall/timemachine") =>
            Action {
              Ok(Json.obj(
                "current" -> Json.obj(
                  "temp" -> 22.2,
                  "humidity" -> 66,
                  "wind_speed" -> 18
                )
              ))
            }
        }
      }{ implicit port =>
        WsTestClient.withClient { wsClient =>
          val weatherClient = new OpenWeatherMapClient(wsClient, config)
          val future = weatherClient.retrieve(Fixture.location, Instant.now)
          val result = Await.result(future, 10.seconds)
          result mustEqual WeatherConditions(22.2, 66, 18)
        }
      }
    }
  }
}
