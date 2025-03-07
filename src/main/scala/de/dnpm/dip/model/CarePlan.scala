package de.dnpm.dip.model


import java.time.LocalDate


trait CarePlan
{
  val id: Id[CarePlan]
  val patient: Reference[Patient]
  val issuedOn: LocalDate
  val reason: Option[Reference[Diagnosis]]
  val therapyRecommendations: Option[List[TherapyRecommendation]]
  val medicationRecommendations: Option[List[MedicationRecommendation[_]]]
  val studyEnrollmentRecommendations: Option[List[StudyEnrollmentRecommendation]]
  val notes: Option[List[String]]
}

