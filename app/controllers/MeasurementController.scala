package controllers

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}
import javax.inject.Inject

import controllers.MeasurementController._
import model.BodyMeasurement
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.mvc._
import services.MeasurementRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class MeasurementController @Inject()(cc: ControllerComponents,
                                      repository: MeasurementRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {


  def create = Action.async(parse.json) { request: Request[JsValue] =>

    request.body.validate[BodyMeasurement] match {
      case JsError(errors) ⇒
        Future.successful(BadRequest(JsError.toJson(errors)))
      case JsSuccess(measurement, _) ⇒
        repository.insert(measurement).map(id => Created)
    }

  }


  def lookup(patientId: Int) = Action.async { implicit request: RequestHeader =>

    (for {

      tipe <- Try(parameter("type"))
      from <- Try(parameter("from").map(parseInstant(startOfDay)))
      to <- Try(parameter("to").map(parseInstant(startOfNextDay)))

    } yield (Option(patientId), tipe, from, to)) match {

      case Failure(parseError) =>
        Future.successful(BadRequest(parseError.getMessage))
      case Success(parsedParameters) =>
        (repository.read _).tupled(parsedParameters).map(toJson(_)).map(Ok(_))
    }

  }
  

}


object MeasurementController {

  private def parameter(key: String)(implicit request: RequestHeader) =
    request.getQueryString(key).map(_.trim).filter(_.nonEmpty)

  private def parseInstant(dayBoundary: LocalDate => LocalDateTime): String => Instant =
    stringToDate.andThen(dayBoundary).andThen(_.toInstant(ZoneOffset.UTC))

  private val stringToDate: String => LocalDate = LocalDate.parse(_: String, DateTimeFormatter.ofPattern("yyyyMMdd"))

  private val startOfDay: LocalDate => LocalDateTime = _.atStartOfDay()

  private val startOfNextDay: LocalDate => LocalDateTime = _.plusDays(1).atStartOfDay()
  
}

