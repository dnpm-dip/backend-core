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


sealed trait Therapy
{
  val id: Id[Therapy]
  val patient: Reference[Patient]
  val recordedOn: LocalDate
  val reason: Option[Reference[Diagnosis]]
  val basedOn: Option[Reference[TherapyRecommendation]]
  val category: Option[Coding[_]]
  val therapyLine: Option[Int]
  val status: Coding[Therapy.Status.Value]
  val statusReason: Option[Coding[Therapy.StatusReason.Value]]
  val period: Option[Period[LocalDate]]
  val notes: Option[List[String]]

/*
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
*/  
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

/*
  object Type
  extends CodedEnum("dnpm-dip/therapy/type")
  with DefaultCodeSystem
  {

    val CH, HO, IM, ZS, SZ, WW, AS, WS, OP, ST, KW, SO = Value
 
    override val display =
      Map(
        CH -> "Chemotherapie",
        HO -> "Hormontherapie",
        IM -> "Immun-/Antikörpertherapie",
        ZS -> "zielgerichtete Substanzen",
        SZ -> "Stammzelltransplantation (inklusive Knochenmarktransplantation)",
        WW -> "Watchful Waiting",
        AS -> "Active Surveillance",
        WS -> "Wait and see",
        OP -> "Operation",
        ST -> "Strahlentherapie",
        KW -> "keine weitere tumorspezifische Therapie empfohlen",
        SO -> "Sonstiges"
      )
 
    implicit val format: Format[Value] =
        Json.formatEnum(this)
  }
*/

  object Status
  extends CodedEnum("dnpm-dip/therapy/status")
  with DefaultCodeSystem
  {

    val NotDone   = Value("not-done")
    val Ongoing   = Value("on-going")
    val Stopped   = Value("stopped")
    val Completed = Value("completed")
//    val Unknown   = Value("unknown")

    override val display =
      Map(
        NotDone   -> "Nicht umgesetzt",
        Ongoing   -> "Laufend",
        Stopped   -> "Abgebrochen",
        Completed -> "Abgeschlossen",
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

    val PaymentRefused                       = Value("payment-refused")
    val PaymentPending                       = Value("payment-pending")
    val PaymentEnded                         = Value("payment-ended")
    val NoIndication                         = Value("no-indication")
    val MedicalReasons                       = Value("medical-reasons")
    val PatientRefusal                       = Value("patient-refusal")
    val PatientWish                          = Value("patient-wish")
    val PatientDeath                         = Value("patient-death")
    val LostToFU                             = Value("lost-to-fu")
    val Remission                            = Value("chronic-remission")
    val Progression                          = Value("progression")
    val Toxicity                             = Value("toxicity")
    val OtherTherapyChosen                   = Value("other-therapy-chosen")
    val Deterioration                        = Value("deterioration")
    val BestSupportiveCare                   = Value("best-supportive-care")
    val RegularCompletion                    = Value("regular-completion")
    val RegularCompletionWithDosageReduction = Value("regular-completion-with-dosage-reduction")
    val RegularCompletionWithSubstanceChange = Value("regular-completion-with-substance-change")
    val Other                                = Value("other")

    override val display =
      Map(
        PaymentRefused                       -> "Kostenübernahme abgelehnt",
        PaymentPending                       -> "Kostenübernahme noch ausstehend",
        PaymentEnded                         -> "Ende der Kostenübernahme",
        NoIndication                         -> "Klinisch keine Indikation",
        MedicalReasons                       -> "Medizinische Gründe",
        PatientRefusal                       -> "Therapie durch Patient abgelehnt",
        PatientWish                          -> "Auf Wunsch des Patienten",
        PatientDeath                         -> "Tod",
        LostToFU                             -> "Lost to follow-up",
        Remission                            -> "Anhaltende Remission",
        Progression                          -> "Progression",
        Toxicity                             -> "Toxizität",
        OtherTherapyChosen                   -> "Wahl einer anderen Therapie durch Behandler",
        Deterioration                        -> "Zustandsverschlechterung",
        BestSupportiveCare                   -> "Best Supportive Care",
        RegularCompletion                    -> "Reguläres Ende",
        RegularCompletionWithDosageReduction -> "Reguläres Ende mit Dosisreduktion",
        RegularCompletionWithSubstanceChange -> "Reguläres Ende mit Substanzwechsel",
        Other                                -> "Weitere Gründe"
      )

  }

}
