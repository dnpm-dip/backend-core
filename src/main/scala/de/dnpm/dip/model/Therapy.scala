package de.dnpm.dip.model


import java.time.LocalDate
import cats.Applicative
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI,
  CodedEnum,
  DefaultCodeSystem,
}


sealed trait Therapy
{
  val id: Id[Therapy]
  val patient: Reference[Patient]
  val indication: Reference[Diagnosis]
  val category: Option[Coding[_]]
  val status: Coding[Therapy.Status.Value]
  val statusReason: Option[Coding[Therapy.StatusReason]]
  val therapyLine: Option[Int]
  val basedOn: Option[Reference[TherapyRecommendation]]
  val recordedOn: Option[LocalDate]
  val period: Option[Period[LocalDate]]
  val note: Option[String]
}


trait MedicationTherapy[Medication] extends Therapy
{
  val medication: Option[Set[Coding[Medication]]]
}

trait Procedure[CS] extends Therapy
{
  val code: Coding[CS]
}



object Therapy
{

  object Status
  extends CodedEnum("dnpm-dip/therapy/status")
  with DefaultCodeSystem
  {

    val NotDone   = Value("not-done")
    val Ongoing   = Value("on-going")
    val Stopped   = Value("stopped")
    val Completed = Value("completed")
    val Unknown   = Value("unknown")

    override val display =
      Map(
        NotDone   -> "Nicht umgesetzt",
        Ongoing   -> "Laufend",
        Stopped   -> "Abgebrochen",
        Completed -> "Abgeschlossen",
        Unknown   -> "Unbekannt"
      )


    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }

  }


  sealed trait StatusReason

  object StatusReason
  {
    implicit val codingSystem: Coding.System[StatusReason] =
      Coding.System[StatusReason]("dnpm-dip/therapy/status-reason")
  }

}
