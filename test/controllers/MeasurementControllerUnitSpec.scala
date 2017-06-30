package controllers

import java.time.Instant
import java.time.format.DateTimeFormatter

import model.BodyMeasurement
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import org.specs2.mock.Mockito
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.MeasurementRepository

import scala.concurrent.Future

class MeasurementControllerUnitSpec extends WordSpec with Mockito with Matchers with BeforeAndAfter {

  private val controllerComponents = stubControllerComponents()
  private implicit val ec = controllerComponents.executionContext

  private val repository = mock[MeasurementRepository]

  before {
    reset(repository)
    repository.read(any, any, any, any) returns Future.successful(Seq.empty)
    repository.insert(any) returns Future.successful(4711L)
  }

  private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")

  private val controller = new MeasurementController(controllerComponents, repository)

  "create" should {

    val measurement = BodyMeasurement(None, 4711, "BloodPressure", 5.5, Instant.from(dateTimeFormatter.parse("2015-05-01T11:00:00Z")))

    val fakeRequest = FakeRequest(POST, "/measurements").withBody[JsValue](Json.toJson(measurement))


    s"create measurement and return $CREATED " in {

      val result: Future[Result] = controller.create()(fakeRequest)

      status(result) should be(201)

      verify(repository).insert(measurement)

    }

   
    s"return $BAD_REQUEST if json is invalid" in {

      val request = fakeRequest.withBody[JsValue](Json.parse("""{ "timestamp": "2015-05-01T13:00:00+02:00" }"""))

      val result: Future[Result] = controller.create()(request)

      status(result) should be(400)
      verifyZeroInteractions(repository)
    }
  }



  "lookup" should {

    "return proper content type header" in {

      val result: Future[Result] = controller.lookup(4711)(FakeRequest("GET", "/measurements/4711"))

      contentType(result).get should be("application/json")

    }

    s"return $OK and lookup by patientId" in {

      val result: Future[Result] = controller.lookup(4711)(FakeRequest("GET", "/measurements/4711"))

      status(result) should be(200)
      verify(repository).read(Some(4711), None, None, None)

    }

    s"return $OK and do not filter if there are no query parameters" in {

      val result: Future[Result] = controller.lookup(4711)(FakeRequest("GET", "/measurements/4711"))

      status(result) should be(200)
      verify(repository).read(Some(4711), None, None, None)
    }

    s"return $OK and do not filter if there are empty query parameters" in {

      val result: Future[Result] = controller.lookup(4711)(FakeRequest("GET", "/measurements/4711?type= &to="))

      status(result) should be(200)
      verify(repository).read(Some(4711), None, None, None)

    }


    s"return $OK and filter by type" in {

      val result: Future[Result] = controller.lookup(4711)(FakeRequest("GET", "/measurements/4711?type=BloodPressure"))

      status(result) should be(200)
      verify(repository).read(Some(4711), Some("BloodPressure"), None, None)

    }

    s"return $OK and filter by time range inclusively" in {

      val result: Future[Result] = controller.lookup(4711)(FakeRequest("GET", "/measurements/4711?from=20150101&to=20170101"))

      status(result) should be(200)

      verify(repository).read(Some(4711), None,
        Some(Instant.from(dateTimeFormatter.parse("2015-01-01T00:00:00Z"))),
        Some(Instant.from(dateTimeFormatter.parse("2017-01-02T00:00:00Z"))))

    }

    s"return $BAD_REQUEST if time range params are not numbers" in {

      val result: Future[Result] = controller.lookup(4711)(FakeRequest("GET", "/measurements/4711?from=abc"))

      status(result) should be(400)
      verifyZeroInteractions(repository)

    }

    s"not handle repository access errors" in {

      repository.read(any,any, any, any) throws new RuntimeException

      assertThrows[RuntimeException] {
        controller.lookup(4711)(FakeRequest("GET", "/measurements/4711?from=20150101"))
      }

    }

  }

}
