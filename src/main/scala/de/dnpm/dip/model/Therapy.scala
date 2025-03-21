package de.dnpm.dip.model


import java.time.LocalDate
import cats.Applicative
import de.dnpm.dip.coding.{
  Coding,
  CodeSystemProvider,
  CodeSystemProviderSPI,
  CodedEnum,
  DefaultCodeSystem,
}


sealed trait Therapy extends Commentable
{
  val id: Id[Therapy]
  val patient: Reference[Patient]
  val recordedOn: LocalDate
  val reason: Option[Reference[Diagnosis]]
  val basedOn: Option[Reference[TherapyRecommendation]]
  val category: Option[Coding[_]]
  val therapyLine: Option[Int]
  val status: Coding[Therapy.Status.Value]
  val statusReason: Option[Coding[_]]
  val period: Option[Period[LocalDate]]

  final def statusValue: Therapy.Status.Value =
    status match {
      case Therapy.Status(s) => s
      case _ => Therapy.Status.Unknown
    }

}

trait SystemicTherapy[Med] extends Therapy
{
  val medication: Option[Set[Coding[Med]]]
}


trait Procedure extends Therapy
{
  val code: Coding[_]
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

}
