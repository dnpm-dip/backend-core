package de.dnpm.dip.model


import java.time.LocalDate


trait PMRecord[
  Diag <: Diagnosis
]
{
 
  val patient: Patient
  val episodes: List[Episode]
  val diagnoses: List[Diag]

  def id = patient.id
}
