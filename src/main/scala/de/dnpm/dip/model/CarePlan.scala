package de.dnpm.dip.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum
}

trait CarePlan extends Commentable
{
  val id: Id[CarePlan]
  val patient: Reference[Patient]
  val issuedOn: LocalDate
  val reason: Option[Reference[Diagnosis]]
  val statusReason: Option[Coding[_]]
  val therapyRecommendations: Option[List[TherapyRecommendation]]
  val medicationRecommendations: Option[List[MedicationRecommendation[_]]]
  val studyEnrollmentRecommendations: Option[List[StudyEnrollmentRecommendation]]
}


object CarePlan
{

  trait NonInclusionReason
  {
    this: CodedEnum =>

    val TargetedDiagnosticsRecommended = Value("targeted-diagnostics-recommended")
    val Pyschosomatic                  = Value("psychosomatic")
    val NotRareDisease                 = Value("not-rare-disease")
    val NonGeneticCause                = Value("non-genetic-cause")
    val Other                          = Value("other")

    protected val defaultDisplay =
      Map(
        TargetedDiagnosticsRecommended -> "Zieldiagnostik empfohlen",
        Pyschosomatic                  -> "Wahrscheinlich psychosomatische Erkrankung",
        NotRareDisease                 -> "Wahrscheinlich häufige Erkrankung",
        NonGeneticCause                -> "Wahrscheinlich nicht genetische Ursache",
        Other                          -> "Anderer Grund"

      )
  }

}

