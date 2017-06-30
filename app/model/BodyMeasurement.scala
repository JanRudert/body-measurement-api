package model

import java.sql
import java.time.Instant

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class BodyMeasurement(id: Option[Long] = None, patientId: Int, tipe: String, value: Double, timestamp: Instant)


object BodyMeasurement {

  implicit val reads: Reads[BodyMeasurement] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "patientId").read[Int] and
      (JsPath \ "type").read[String] and
      (JsPath \ "value").read[Double] and
      (JsPath \ "timestamp").read[Instant]
    ) (BodyMeasurement.apply (_, _, _, _, _:Instant))


  implicit val writes: Writes[BodyMeasurement] = (
    (JsPath \ "id").writeNullable[Long] and
      (JsPath \ "patientId").write[Int] and
      (JsPath \ "type").write[String] and
      (JsPath \ "value").write[Double] and
      (JsPath \ "timestamp").write[Instant]
    ) (unlift(BodyMeasurement.unapply))


  def forSlick(measurement: BodyMeasurement): Option[(Long, Int, String, Double, sql.Timestamp)] = {
    import measurement._
    Some((measurement.id.getOrElse(0), patientId, tipe, value, sql.Timestamp.from(timestamp)))
  }

  def fromSlick(id: Long, patientId: Int, tipe: String, value: Double, timestamp: sql.Timestamp): BodyMeasurement =
    BodyMeasurement(Some(id), patientId, tipe, value, timestamp.toInstant)
}


