package de.dnpm.dip.model


import java.time.LocalDate
import java.time.temporal.ChronoUnit
import ChronoUnit.YEARS
import scala.util.chaining._
import de.dnpm.dip.coding.Coding
import play.api.libs.json.{
  Json,
  Format
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

  def ageIn(ch: ChronoUnit): Long =
    ch.between(
      birthDate,
      dateOfDeath.getOrElse(LocalDate.now)
    )

  lazy val age: Long =
    ageIn(YEARS)


  lazy val vitalStatus: Coding[VitalStatus.Value] =
    dateOfDeath
      .map(_ => VitalStatus.Deceased)
      .getOrElse(VitalStatus.Alive)
      .pipe(Coding(_).complete)
}


object Patient
{
  implicit val format: Format[Patient] = 
    Json.format[Patient]
}
