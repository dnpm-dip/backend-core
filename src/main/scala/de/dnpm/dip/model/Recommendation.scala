package de.dnpm.dip.model


import java.time.LocalDate
import cats.Applicative
import cats.data.NonEmptyList
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI
}


trait Recommendation
{
  val id: Id[Recommendation]
  val patient: Reference[Patient]
  val issuedOn: LocalDate
  val supportingVariants: Option[List[GeneAlterationReference[_]]]
}

object Recommendation
{

  object Priority
  extends CodedEnum("dnpm-dip/recommendation/priority")
  with DefaultCodeSystem
  {
    val One   = Value("1")
    val Two   = Value("2")
    val Three = Value("3")
    val Four  = Value("4")

    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }

  }

}


trait TherapyRecommendation extends Recommendation
{
  val reason: Option[Reference[Diagnosis]]
  val priority: Coding[Recommendation.Priority.Value]
}

trait MedicationRecommendation[M] extends TherapyRecommendation
{
  val medication: Set[Coding[M]]
}


trait StudyEnrollmentRecommendation extends Recommendation
{
//  val study: ExternalReference[Study,Study.Registries]
  val study: NonEmptyList[ExternalReference[Study,Study.Registries]]
}

