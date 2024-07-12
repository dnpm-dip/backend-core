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
  val indication: Option[Reference[Diagnosis]]
  val recordedOn: LocalDate
  val basedOn: Option[Reference[TherapyRecommendation]]
  val category: Option[Coding[_]]
  val status: Coding[Therapy.Status.Value]
  val statusReason: Option[Coding[Therapy.StatusReason.Value]]
  val therapyLine: Option[Int]
  val period: Option[Period[LocalDate]]
  val notes: Option[String]


  final def statusValue: Therapy.Status.Value =
    status match {
      case Therapy.Status(s) => s
      case _ => Therapy.Status.Unknown
    }

  final def statusReasonValue: Therapy.StatusReason.Value =
    statusReason match {
      case Some(Therapy.StatusReason(s)) => s
      case _ => Therapy.StatusReason.Unknown
    }
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
  extends CodedEnum("dnpm-dip/therapy/status-reason")
  with DefaultCodeSystem
  {

    val PaymentRefused      = Value("payment-refused")
    val PaymentPending      = Value("payment-pending")
    val PaymentEnded        = Value("payment-ended")
    val NoIndication        = Value("no-indication")
    val MedicalReason       = Value("medical-reason")
    val PatientRefusal      = Value("patient-refusal")
    val PatientWish         = Value("patient-wish")
    val PatientDeath        = Value("patient-death")
    val LostToFU            = Value("lost-to-fu")
    val Remission           = Value("chronic-remission")
    val Progression         = Value("progression")
    val Toxicity            = Value("toxicity")
    val OtherTherapyChosen  = Value("other-therapy-chosen")
    val ContinuedExternally = Value("continued-externally")
    val StateDeterioration  = Value("deterioration")
    val Other               = Value("other")
    val Unknown             = Value("unknown")

    override val display =
      Map(
        PaymentRefused      -> "Kostenübernahme abgelehnt",
        PaymentPending      -> "Kostenübernahme noch ausstehend",
        PaymentEnded        -> "Ende der Kostenübernahme",
        NoIndication        -> "Klinisch keine Indikation",
        MedicalReason       -> "Medizinische Gründe",
        PatientRefusal      -> "Therapie durch Patient abgelehnt",
        PatientWish         -> "Auf Wunsch des Patienten",
        PatientDeath        -> "Tod",
        LostToFU            -> "Lost to follow-up",
        Remission           -> "Anhaltende Remission",
        Progression         -> "Progression",
        Toxicity            -> "Toxizität",
        OtherTherapyChosen  -> "Wahl einer anderen Therapie durch Behandler",
        ContinuedExternally -> "Weiterbehandlung extern",
        StateDeterioration  -> "Zustandsverschlechterung",
        Other               -> "Weitere Gründe",
        Unknown             -> "Unbekannt"
      )

  }

}
