package de.dnpm.dip.model


import java.time.LocalDate


trait CarePlan
{
  val id: Id[CarePlan]
  val patient: Reference[Patient]
  val issuedOn: LocalDate
  val indication: Option[Reference[Diagnosis]]
  val therapyRecommendations: Option[List[TherapyRecommendation]]
  val medicationRecommendations: Option[List[MedicationRecommendation[_]]]
//  val studyEnrollmentRecommendation: Option[StudyEnrollmentRecommendation]
  val notes: Option[String]
}

