package de.dnpm.dip.model


import java.time.LocalDate
import java.time.temporal.ChronoUnit
import ChronoUnit.YEARS
import scala.util.chaining._
import de.dnpm.dip.coding.Coding
import play.api.libs.json.{
  Json,
  OFormat
}
import de.dnpm.dip.util.Completer.syntax._


final case class Patient
(
  id: Id[Patient],
  gender: Coding[Gender.Value],
  birthDate: LocalDate,
  dateOfDeath: Option[LocalDate],
  managingSite: Option[Coding[Site]],
  healthInsurance: Option[Reference[Organization]]
)
{

  def ageIn(ch: ChronoUnit): Age =
    Age(
      ch.between(
        birthDate,
        dateOfDeath.getOrElse(LocalDate.now)
      )
      .toDouble,
      UnitOfTime.of(ch)
    )

  lazy val age: Age =
    ageIn(YEARS)


  lazy val vitalStatus: Coding[VitalStatus.Value] =
    dateOfDeath
      .map(_ => VitalStatus.Deceased)
      .getOrElse(VitalStatus.Alive)
      .pipe(Coding(_).complete)
}


object Patient
{
  implicit val format: OFormat[Patient] = 
    Json.format[Patient]
}
