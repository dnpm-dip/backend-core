package de.dnpm.dip.model


import cats.data.NonEmptyList


trait PatientRecord
{

  val patient: Patient

  val episodesOfCare: NonEmptyList[EpisodeOfCare]

  val ngsReports: Option[List[NGSReport]]

  val carePlans: Option[List[CarePlan]]

  val therapies: Option[List[History[MedicationTherapy[_]]]]


  final def id: Id[Patient] =
    patient.id

}
