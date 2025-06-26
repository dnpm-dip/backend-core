package de.dnpm.dip.model


import cats.data.NonEmptyList


trait PatientRecord
{

  val patient: Patient

  val episodesOfCare: NonEmptyList[EpisodeOfCare]

  val diagnoses: NonEmptyList[Diagnosis]

  val ngsReports: Option[List[NGSReport]]

//  val carePlans: Option[List[CarePlan]]

  val followUps: Option[List[FollowUp]]

  val systemicTherapies: Option[List[History[SystemicTherapy[_]]]]


  final def id: Id[Patient] =
    patient.id


  def getCarePlans: List[CarePlan]

}
