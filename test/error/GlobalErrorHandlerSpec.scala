package error

import fixture.Fixture
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.{PlaySpec, WsScalaTestClient}
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

class GlobalErrorHandlerSpec
  extends PlaySpec
    with GuiceOneServerPerSuite
    with WsScalaTestClient {

  implicit val wsClient = app.injector.instanceOf[WSClient]

  "The application" should {
    "respond 400 bad request" in {
      val response = await {
        wsUrl("/records")
          .withQueryStringParameters("filter" -> "invalid_filter")
          .withHttpHeaders(Fixture.adminAuthHeader.toSimpleMap.toSeq: _*)
          .get()
      }
      response.status mustBe BAD_REQUEST
      response.contentType mustBe JSON
    }
  }
}
