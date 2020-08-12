package record

import domain.Distance
import fixture.Fixture.adminRequest
import fixture.{BaseSpec, Fixture}
import java.time.temporal.ChronoUnit
import java.time.{Duration, Instant}
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RecordFunctionalSpec extends BaseSpec {

  import dbConfig.profile.api._

  val recordController = instanceOf[RecordController]
  val recordDao = instanceOf[RecordDao]

  "create" should {
    "create record for owner" in {
      val requestBody =
        RecordDto(
          None,
          Instant.now(),
          Distance(3500),
          Duration.ofMinutes(15),
          Fixture.location)

      val request =
        FakeRequest()
          .withHeaders(Fixture.hannahAuthHeader)
          .withBody(requestBody)

      val response = recordController.create()(request)

      status(response) mustEqual CREATED
      val json = contentAsJson(response)
      (json \ "userId").as[Long] mustEqual Fixture.hannah.id

      val recordId = (json \ "id").as[Long]
      val result = execute(recordDao.records.filter(_.id === recordId))
      result must have size 1
      result.head.userId mustEqual Fixture.hannah.id
      result.head.weather.map(_.humidity) mustEqual Some(66)
    }
    "not fail when weather API fails" in {
      val requestBody =
        RecordDto(
          None,
          Instant.now().minus(6, ChronoUnit.DAYS),
          Distance(3500),
          Duration.ofMinutes(15),
          Fixture.location)

      val request =
        FakeRequest()
          .withHeaders(Fixture.hannahAuthHeader)
          .withBody(requestBody)

      val response = recordController.create()(request)
      val json = contentAsJson(response)
      (json \ "userId").as[Long] mustEqual Fixture.hannah.id

      val recordId = (json \ "id").as[Long]
      val result = execute(recordDao.records.filter(_.id === recordId))
      result must have size 1
      result.head.userId mustEqual Fixture.hannah.id
      result.head.weather mustBe empty
      status(response) mustEqual CREATED
    }
    "force record to owner" in {
      val requestBody =
        RecordDto(
          Some(Fixture.jonas.id),
          Instant.now(),
          Distance(3500),
          Duration.ofMinutes(15),
          Fixture.location)

      val request =
        FakeRequest()
          .withHeaders(Fixture.hannahAuthHeader)
          .withBody(requestBody)

      val response = recordController.create()(request)

      status(response) mustEqual CREATED
      val json = contentAsJson(response)
      (json \ "userId").as[Long] mustEqual Fixture.hannah.id
    }
    "allow admin to create record to other user" in {
      val requestBody =
        RecordDto(
          Some(Fixture.jonas.id),
          Instant.now(),
          Distance(3500),
          Duration.ofMinutes(15),
          Fixture.location)

      val request =
        FakeRequest()
          .withHeaders(Fixture.adminAuthHeader)
          .withBody(requestBody)

      val response = recordController.create()(request)

      status(response) mustEqual CREATED
      val json = contentAsJson(response)
      (json \ "userId").as[Long] mustEqual Fixture.jonas.id
    }
  }

  "retrieve" should {
    "filter records by duration" in {
      val request = FakeRequest().withHeaders(Fixture.hannahAuthHeader)
      val filter = Some("duration eq 'PT40M' and distance ne 5000")
      val limit = None
      val offset = None

      val response = recordController.retrieve(filter, limit, offset)(request)
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 1
      (json \ "results").as[Seq[JsObject]].foreach { result =>
        (result \ "duration").as[Duration] mustEqual Duration.ofMinutes(40)
        (result \ "distance").as[Int] mustNot equal(5000)
      }
    }
    "filter complex query" in {
      val request = FakeRequest().withHeaders(Fixture.hannahAuthHeader)
      val filter = Some(
        "((duration lt 'PT40M' or distance lt 5000) and date ne '2020-08-08T00:00:00Z') and " +
          "date lt '2020-08-08T00:00:00Z' and (duration ne 'PT25M' and lon gt 10.1)")
      val limit = None
      val offset = None

      val response = recordController.retrieve(filter, limit, offset)(request)
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 1
      (json \ "results").as[Seq[JsObject]].foreach { result =>
        (result \ "duration").as[Duration] must be < Duration.ofMinutes(40)
        (result \ "duration").as[Duration] must not equal Duration.ofMinutes(25)
        (result \ "date").as[Instant] must be < Instant.parse("2020-08-08T00:00:00Z")
      }
    }
    "skip offset results" in {
      val request = FakeRequest().withHeaders(Fixture.hannahAuthHeader)
      val filter = Some("duration eq 'PT40M' and distance ne 5000")
      val limit = None
      val offset = Some(1)

      val response = recordController.retrieve(filter, limit, offset)(request)
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 0
      (json \ "results").as[Seq[JsObject]] mustBe empty
    }
    "limit results" in {
      val request = FakeRequest().withHeaders(Fixture.hannahAuthHeader)
      val filter = Some("duration eq 'PT40M' and distance ne 5000")
      val limit = Some(1)
      val offset = None

      val response = recordController.retrieve(filter, limit, offset)(request)
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 1
      (json \ "results").as[Seq[JsObject]].foreach { result =>
        (result \ "duration").as[Duration] mustEqual Duration.ofMinutes(40)
        (result \ "distance").as[Int] mustNot equal(5000)
      }
    }
    "allow admin to retrieve other users' records" in {
      val request = FakeRequest().withHeaders(Fixture.adminAuthHeader)
      val filter =
        Some("duration eq 'PT40M' and (distance ne 5000 and lat lt 50)")
      val limit = None
      val offset = None

      val response = recordController.retrieve(filter, limit, offset)(request)
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 2
      (json \ "results").as[Seq[JsObject]].foreach { result =>
        (result \ "duration").as[Duration] mustEqual Duration.ofMinutes(40)
        (result \ "distance").as[Int] must (not equal 500)
        (result \ "location" \ "lat").as[Double] must be < 50.0
      }
    }
  }

  "retrieveReport" should {
    "filter records by user" in {
      val request = FakeRequest().withHeaders(Fixture.hannahAuthHeader)
      val filter = None
      val limit = Some(1)
      val offset = None

      val response = recordController.retrieveReport(None, filter, limit, offset)(request)
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 1
      val report = (json \ "results").as[Seq[JsObject]].head
      (report \ "weekOfYear").as[Int] mustEqual 32
      (report \ "averageSpeed").as[Double] must (be > 2.97 and be < 2.98)
      (report \ "total").as[Int] mustEqual 31500
    }
    "filter records by average speed" in {
      val request = Fixture.adminRequest
      val filter = Some("avgSpeed gt 3.0 and avgSpeed lt 5 and avgSpeed ne 2.5 or avgSpeed eq 2.12")
      val limit = None
      val offset = None

      val response = recordController.retrieveReport(None, filter, limit, offset)(request)
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 2
      (json \ "results").as[Seq[JsObject]].foreach { report =>
        (report \ "averageSpeed").as[Double] must (be > 3.0 and be < 5.0 and not equal 2.5)
      }
    }
    "filter records by total distance" in {
      val request = Fixture.adminRequest
      val filter = Some("distance gt 32000 and distance lt 40000 and distance ne 32120 or distance eq 0")
      val limit = None
      val offset = None

      val response = recordController.retrieveReport(None, filter, limit, offset)(request)
      val json = contentAsJson(response)

      (json \ "count").as[Int] mustEqual 1
      val report = (json \ "results").as[Seq[JsObject]].head
      (report \ "total").as[Int] must (be > 32000 and be < 40000 and not equal 32120)
    }
  }

  "update" should {
    "update record" in {
      val recordId = 1L
      val updatedRecord =
        RecordDto(
          None,
          Instant.now(),
          Distance(4990),
          Duration.ofMinutes(30),
          Fixture.location)
      val request =
        FakeRequest()
          .withHeaders(Fixture.hannahAuthHeader)
          .withBody(updatedRecord)

      val response = recordController.update(recordId)(request)

      status(response) mustBe NO_CONTENT
      val result = execute(recordDao.records.filter(_.id === recordId))
      result must have size 1
      result.head.distance mustEqual updatedRecord.distance
    }
  }

  "delete" should {
    "delete record" in {
      val recordId = 1L
      val request = FakeRequest().withHeaders(Fixture.hannahAuthHeader)

      val response = recordController.delete(recordId)(request)

      status(response) mustEqual NO_CONTENT
      val result = execute(recordDao.records.filter(_.id === recordId))
      result mustBe empty
    }
    "allow admin to delete any record" in {
      val recordId = 2L

      val response = recordController.delete(recordId)(adminRequest)

      status(response) mustEqual NO_CONTENT
      val result = execute(recordDao.records.filter(_.id === recordId))
      result mustBe empty
    }
  }
}
