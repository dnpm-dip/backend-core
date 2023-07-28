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



trait Episode
{
  val id: Id[Episode]
  val patient: Reference[Patient]
  val period: Period[LocalDate]
  val status: Coding[Episode.Status.Value]
  val diagnoses: List[Reference[Diagnosis]]
}


object Episode
{

  object Status
  extends CodedEnum("episode/status")
  with DefaultCodeSystem
  {

    val Planned   = Value("planned")
    val Active    = Value("active")
    val Finished  = Value("finished")
    val Cancelled = Value("cancelled")


    override val display = {
      case Planned    => "In Vorbereitung"
      case Active     => "Laufend"
      case Finished   => "Abgeschlossen"
      case Cancelled  => "Abgebrochen"
    }

    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }

  }


}
