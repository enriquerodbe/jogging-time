package report

import domain.{Distance, Speed}
import fixture.Fixture.{adminRequest, hannahAuthHeader, initialRecords, mikkel}
import fixture.{BaseSpec, Fixture}
import java.time.Instant
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._

class AverageReportFunctionalSpec extends BaseSpec {

  val averageReportController = instanceOf[AverageReportController]
  val averageReportDao = instanceOf[AverageReportDao]

  "create" should {
    "create reports for all users" in {
      val response = averageReportController.create(5)(Fixture.adminRequest)

      status(response) mustEqual CREATED
      val json = contentAsJson(response).as[Seq[JsObject]]

      json must have size 3
      val result = execute(averageReportDao.averageReports)
      result must have size 3

      val mikkelReports = result.filter(_.userId == mikkel.id)
      mikkelReports must have size 1

      val mikkelRecords = initialRecords.filter(_.userId == mikkel.id)
      val speedsSum = mikkelRecords.map(
        r => r.distance.value.toDouble / r.duration.getSeconds).sum
      val avgSpeed = speedsSum / mikkelRecords.size
      mikkelReports.head.averageSpeed mustEqual Speed(avgSpeed)

      val distancesSum = mikkelRecords.map(_.distance.value).sum
      val avgDistance = distancesSum / mikkelRecords.size
      mikkelReports.head.averageDistance mustEqual Distance(avgDistance)
    }
    "not create reports again" in {
      val response = averageReportController.create(5)(Fixture.adminRequest)

      status(response) mustEqual CREATED
      val json = contentAsJson(response).as[Seq[JsObject]]

      json must have size 0
      val result = execute(averageReportDao.averageReports)
      result must have size 3
    }
  }

  "retrieve" should {
    "filter reports by average distance" in {
      val filter = "avgDistance eq 5000"
      val userId = None
      val limit = None
      val offset = None

      val response =
        averageReportController.retrieve(filter, userId, limit, offset)(adminRequest)

      status(response) mustEqual OK
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 1
      (json \ "results").as[Seq[JsObject]].foreach { result =>
        (result \ "averageDistance").as[Double] mustEqual 5000.0
      }
    }
    "limit results to user" in {
      val filter = "avgDistance eq 5000"
      val userId = None
      val limit = None
      val offset = None
      val request = FakeRequest().withHeaders(hannahAuthHeader)

      val response =
        averageReportController.retrieve(filter, userId, limit, offset)(request)

      status(response) mustEqual OK
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 0
    }
    "skipp offset results" in {
      val filter = "date gt '2020-07-10T00:00:00Z'"
      val userId = None
      val limit = None
      val offset = Some(2)

      val response =
        averageReportController.retrieve(filter, userId, limit, offset)(adminRequest)

      status(response) mustEqual OK
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 1
      (json \ "results").as[Seq[JsObject]].foreach { result =>
        (result \ "date").as[Instant] must be > Instant.parse("2020-07-10T00:00:00Z")
      }
    }
    "limit results" in {
      val filter = "avgSpeed gt 2.0"
      val userId = None
      val limit = Some(1)
      val offset = None

      val response =
        averageReportController.retrieve(filter, userId, limit, offset)(adminRequest)

      status(response) mustEqual OK
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 1
      (json \ "results").as[Seq[JsObject]].foreach { result =>
        (result \ "averageSpeed").as[Double] must be > 2.0
      }
    }
  }
}
