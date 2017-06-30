package services

import java.sql
import java.time.Instant
import javax.inject.Inject

import model.BodyMeasurement
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class MeasurementRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val Measurements = TableQuery[MeasurementsTable]

  def insert(measurement: BodyMeasurement): Future[Long] = db.run(Measurements returning Measurements.map(_.id) += measurement)

  def read(patientId: Option[Int], tipe: Option[String], from: Option[Instant], to: Option[Instant]): Future[Seq[BodyMeasurement]] = db.run(
    Measurements.filter(measurement =>
      List(
        patientId.map(measurement.patientId === _),
        tipe.map(measurement.tipe === _),
        from.map(measurement.timestamp > sql.Timestamp.from(_)),
          to.map(measurement.timestamp < sql.Timestamp.from(_))
      ).collect({ case Some(criteria) => criteria }).reduceLeftOption(_ && _).getOrElse(false: Rep[Boolean]))
      .result)



  private class MeasurementsTable(tag: Tag) extends Table[BodyMeasurement](tag, "BODY_MEASUREMENT") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def patientId = column[Int]("PATIENT_ID")

    def tipe = column[String]("TYPE")

    def value = column[Double]("VALUE")

    def timestamp = column[sql.Timestamp]("TIMESTAMP")

    def * = (id, patientId, tipe, value, timestamp) <> ((BodyMeasurement.fromSlick _).tupled, BodyMeasurement.forSlick)
  }

}
