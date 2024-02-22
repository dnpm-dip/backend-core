package de.dnpm.dip.model


import java.time.LocalDate


trait CarePlan
{
  val id: Id[CarePlan]
  val patient: Reference[Patient]
  val issuedOn: LocalDate
  val indication: Reference[Diagnosis]
  val protocol: Option[String]
  val medicationRecommendations: Option[List[MedicationRecommendation[_]]]
}

