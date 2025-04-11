package de.dnpm.dip.model


import java.time.LocalDate
import play.api.libs.json.{
  Json,
  OFormat
}
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem
}


final case class FollowUp
(
  date: LocalDate,
  patient: Reference[Patient],
  lastContactDate: Option[LocalDate],
  patientStatus: Option[Coding[FollowUp.PatientStatus.Value]]
)


object FollowUp
{

  object PatientStatus
  extends CodedEnum("dnpm-dip/follow-up/patient-status")
  with DefaultCodeSystem
  {
    
    val LostToFU = Value("lost-to-fu")
    
    override val display =
      Map(
        LostToFU -> "Lost to follow-up"
      )
  }


  implicit val format: OFormat[FollowUp] =
    Json.format[FollowUp]
}
