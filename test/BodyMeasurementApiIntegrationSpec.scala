import java.time.Instant
import java.time.format.DateTimeFormatter

import model.BodyMeasurement
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import org.specs2.mock.Mockito
import play.api.libs.json.Json
import play.api.libs.ws._
import play.api.test.Helpers._
import play.mvc.Http.HeaderNames
import services.MeasurementRepository

class BodyMeasurementApiIntegrationSpec extends WordSpec with Mockito with Matchers with GuiceOneServerPerTest {

  private val controllerComponents = stubControllerComponents()
  private lazy implicit val ec = controllerComponents.executionContext

  private def wsClient = app.injector.instanceOf[WSClient]

  private lazy val appDomain = s"localhost:$port"


  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")

  private val testData = Seq(
    BodyMeasurement(None, 4714, "Pulse", 10.0, Instant.from(formatter.parse("2015-05-01T11:00:00Z"))),
    BodyMeasurement(None, 4714, "BloodPressure", 120.7, Instant.from(formatter.parse("2016-05-01T11:00:00Z"))),
    BodyMeasurement(None, 4714, "BloodPressure", 120.8, Instant.from(formatter.parse("2016-12-01T11:00:00Z"))),
    BodyMeasurement(None, 4714, "Temperature", 37.9, Instant.from(formatter.parse("2016-07-01T11:00:00Z")))
  )


  "create" should {

    lazy val url = s"http://$appDomain/measurements"

    "create new measurement" in {

      val response = await(wsClient.url(url).post(Json.parse(
        """{ "timestamp": "2015-05-01T13:00:00+02:00", "patientId": 4711, "type": "BloodPressure", "value": 5.5 }""")))

      response.status shouldBe CREATED

    }

  }


  "lookup" should {

    lazy val url = s"http://$appDomain/measurements/4714"

    "get measurements filtered by type" in {

      insertTestData()

      val response = await(wsClient.url(url).withQueryStringParameters("type" -> "BloodPressure").get())

      response.status shouldBe OK

      val array = response.json.as[List[BodyMeasurement]]
      array should have size 2
      array.forall(_.tipe == "BloodPressure") shouldBe true

    }

    "get measurements filtered by type and time range" in {

      insertTestData()

      val response = await(wsClient.url(url).withQueryStringParameters("type" -> "BloodPressure", "from" -> "20160601", "to" -> "20180101").get())

      response.status shouldBe OK

      val array = response.json.as[List[BodyMeasurement]]
      array should have size 1
      array.head.tipe shouldBe "BloodPressure"

    }

    "set content type header on response" in {
      val response = await(wsClient.url(url).get())
      response.header(HeaderNames.CONTENT_TYPE) shouldBe Some("application/json")
    }

    "not set cache headers on response" in {
      val response = await(wsClient.url(url).get())
      response.headerValues(HeaderNames.CACHE_CONTROL) shouldBe empty
    }


  }

  private def insertTestData(): Unit = {
    val repository: MeasurementRepository = app.injector.instanceOf[MeasurementRepository]
    testData.foreach(repository.insert)
  }
}
