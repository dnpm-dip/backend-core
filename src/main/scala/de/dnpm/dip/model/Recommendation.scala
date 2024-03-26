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



trait Recommendation
{
  val id: Id[Recommendation]
  val patient: Reference[Patient]
  val issuedOn: LocalDate
}


trait TherapyRecommendation extends Recommendation
{
  val indication: Reference[Diagnosis]
  val priority: Coding[TherapyRecommendation.Priority.Value]
}


trait MedicationRecommendation[Medication] extends TherapyRecommendation
{
  val medication: Set[Coding[Medication]]
}

object TherapyRecommendation
{

  object Priority
  extends CodedEnum("dnpm-dip/therapy-recommendation/priority")
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

