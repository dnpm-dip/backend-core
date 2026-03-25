package de.dnpm.dip.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem
}


trait CarePlan extends Commentable
{
  val id: Id[CarePlan]
  val patient: Reference[Patient]
  val issuedOn: LocalDate
  val reason: Option[Reference[Diagnosis]]
  val boardType: Option[Coding[CarePlan.BoardType.Value]]
  val noSequencingPerformedReason: Option[Coding[CarePlan.NoSequencingPerformedReason.Value]]
  val therapyRecommendations: Option[List[TherapyRecommendation]]
  val medicationRecommendations: Option[List[MedicationRecommendation[_]]]
  val studyEnrollmentRecommendations: Option[List[StudyEnrollmentRecommendation]]
}

object CarePlan
{

  object BoardType
  extends CodedEnum("dnpm-dip/careplan/board-type")
  with DefaultCodeSystem
  {

    val IndicationBoard = Value("indication-board")
    val TherapyBoard    = Value("therapy-board")

    override val display =
      Map(
        IndicationBoard -> "Indikations-Board",
        TherapyBoard    -> "Befund-Board" 
      )

  }


  object NoSequencingPerformedReason
  extends CodedEnum("dnpm-dip/careplan/no-sequencing-performed-reason")
  with DefaultCodeSystem
  {

    val TargetedDiagnosticsRecommended = Value("targeted-diagnostics-recommended")
    val Pyschosomatic                  = Value("psychosomatic")
    val NotRareDisease                 = Value("not-rare-disease")
    val NonGeneticCause                = Value("non-genetic-cause")
    val Other                          = Value("other")

    override val display =
      Map(
        TargetedDiagnosticsRecommended -> "Zieldiagnostik empfohlen",
        Pyschosomatic                  -> "Wahrscheinlich psychosomatische Erkrankung",
        NotRareDisease                 -> "Wahrscheinlich häufige Erkrankung",
        NonGeneticCause                -> "Wahrscheinlich nicht genetische Ursache",
        Other                          -> "Anderer Grund"
      )
  }

}
