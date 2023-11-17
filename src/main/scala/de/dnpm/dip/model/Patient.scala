package de.dnpm.dip.model


import java.time.LocalDate
import java.time.temporal.ChronoUnit
import ChronoUnit.YEARS
import scala.util.chaining._
import de.dnpm.dip.coding.Coding
import play.api.libs.json.{
  Json,
  Reads,
  OWrites
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

  implicit val reads: Reads[Patient] = 
    Json.reads[Patient]

  implicit val writes: OWrites[Patient] = 
    OWrites[Patient]{
      pat =>
        Json.obj(
          "id"              -> pat.id,
          "gender"          -> pat.gender,
          "birthDate"       -> pat.birthDate,
          "dateOfDeath"     -> pat.dateOfDeath,
          "managingSite"    -> pat.managingSite,
          "healthInsurance" -> pat.healthInsurance,
          "age"             -> pat.age,
          "vitalStatus"     -> pat.vitalStatus
        )
    }

}
