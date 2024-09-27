package de.dnpm.dip.model


import java.time.LocalDate
import cats.Applicative
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodedEnum,
  DefaultCodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI
}
import play.api.libs.json.{
  Json,
  OFormat
}


trait EpisodeOfCare
{
  val id: Id[EpisodeOfCare]
  val patient: Reference[Patient]
  val period: Period[LocalDate]
  val diagnoses: Option[List[Reference[Diagnosis]]]
}

