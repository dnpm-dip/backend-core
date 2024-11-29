package de.dnpm.dip.model


import java.time.LocalDate


trait EpisodeOfCare
{
  val id: Id[EpisodeOfCare]
  val patient: Reference[Patient]
  val period: Period[LocalDate]
  val diagnoses: Option[List[Reference[Diagnosis]]]
}

