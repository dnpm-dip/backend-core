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
import de.dnpm.dip.coding.icd.ICD10GM


trait Diagnosis
{
  val id: Id[Diagnosis]
  val patient: Reference[Patient]
  val recordedOn: Option[LocalDate]
}


object GuidelineTreatmentStatus
extends CodedEnum("dnpm-dip/diagnosis/guideline-therapy/status")
with DefaultCodeSystem
{

  val Exhaustive            = Value("exhausted")
  val NonExhaustive         = Value("non-exhausted")
  val Impossible            = Value("impossible")
  val NoGuidelinesAvailable = Value("no-guidelines-available")
  val Unknown               = Value("unknown")

  override val display =
    Map(
      Exhaustive            -> "Leitlinien ausgeschöpft",
      NonExhaustive         -> "Leitlinien nicht ausgeschöpft",
      Impossible            -> "Leitlinientherapie nicht möglich",
      NoGuidelinesAvailable -> "Keine Leitlinien vorhanden",
      Unknown               -> "Unbekannt"
    )

  final class ProviderSPI extends CodeSystemProviderSPI
  {
    override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
      new Provider.Facade[F]
  }
}

