package de.dnpm.dip.model


import java.time.LocalDate
import java.time.temporal.ChronoUnit
import ChronoUnit.YEARS
import scala.util.chaining._
import de.dnpm.dip.coding.Coding
import play.api.libs.json.{
  Json,
  Reads,
  OFormat,
  OWrites
}


final case class Patient
(
  id: Id[Patient],
  gender: Coding[Gender.Value],
  birthDate: LocalDate,
  dateOfDeath: Option[LocalDate],
  managingSite: Option[Coding[Site]],
  healthInsurance: Patient.Insurance,
  address: Option[Address]
)
{

  def ageOnDate(
    date: LocalDate,
    ch: ChronoUnit = YEARS
  ): Age = {

    // Use the minimum of date of death (if defined) and given date as 'reference date',
    // as the age of Patient who died before the given date is defined by his date of death
    val refDate =
      dateOfDeath.map(Ordering[LocalDate].min(_,date))
        .getOrElse(date)

    Age(
      ch.between(birthDate,refDate).toDouble,
      UnitOfTime.of(ch)
    )
  }

  def ageIn(ch: ChronoUnit): Age =
    ageOnDate(dateOfDeath.getOrElse(LocalDate.now),ch)

  def ageIn(t: UnitOfTime): Age =
    ageIn(UnitOfTime.chronoUnit(t))

  def age: Age =
    ageIn(YEARS)


  lazy val vitalStatus: Coding[VitalStatus.Value] =
    dateOfDeath
      .map(_ => VitalStatus.Deceased)
      .getOrElse(VitalStatus.Alive)
      .pipe(Coding(_))

}


object Patient
{

  final case class Insurance
  (
    `type`: Coding[HealthInsurance.Type.Value],
    reference: Option[Reference[HealthInsurance]]
  )


  implicit val formatInsurance: OFormat[Insurance] =
    Json.format[Insurance]

  implicit val reads: Reads[Patient] = 
    Json.reads[Patient]

  implicit val writes: OWrites[Patient] = {

    val w = Json.writes[Patient]

    OWrites[Patient](
      pat =>
        w.writes(pat) +
          ("age"         -> Json.toJson(pat.age)) +
          ("vitalStatus" -> Json.toJson(pat.vitalStatus))
    )
  }

}
