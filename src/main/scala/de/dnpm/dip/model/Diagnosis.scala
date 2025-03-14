package de.dnpm.dip.model


import java.time.LocalDate


trait Diagnosis extends Commentable
{
  val id: Id[Diagnosis]
  val patient: Reference[Patient]
  val recordedOn: Option[LocalDate]
}

