package de.dnpm.dip.model


import cats.data.NonEmptyList
import play.api.libs.json.JsObject


trait PatientRecord
{

  val patient: Patient

  val consent: JsObject   // leave unstructured (for now)

  val episodes: NonEmptyList[Episode]


  final def id: Id[Patient] =
    patient.id

}
