package de.dnpm.dip.model


import java.time.LocalDate
import cats.Applicative
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI
}
import de.dnpm.dip.coding.icd.ICD10GM


trait FamilyMemberDiagnosis
{
  val id: Id[FamilyMemberDiagnosis]
  val patient: Reference[Patient]
  val recordedOn: Option[LocalDate]
  val relationship: Coding[Relationship.Value]
  val code: Coding[ICD10GM]
}

object Relationship
extends CodedEnum("relationship-type")
with DefaultCodeSystem
{

  val FAMMEB, EXT = Value

  override val display = {
    case FAMMEB => "Erstgradig Verwandter"
    case EXT    => "Zweitgradig Verwandter"
  }

  final class ProviderSPI extends CodeSystemProviderSPI
  {
    override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
      new Provider.Facade[F]
  }

}
