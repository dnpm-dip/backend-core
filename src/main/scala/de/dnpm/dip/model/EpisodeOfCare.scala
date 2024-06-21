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
import play.api.libs.json.{
  Json,
  OFormat
}


// Transfer Transaction Number (Transfer-Vorgangs-Nummer)
sealed trait TransferTAN
object TransferTAN
{
  implicit val codingSystem: Coding.System[TransferTAN] =
    Coding.System[TransferTAN]("mvh/transfer-vorgangs-nummer")
}


trait EpisodeOfCare
{
  val id: Id[EpisodeOfCare]
  val transferTan: Option[Id[TransferTAN]]  //TODO: Make required 
  val patient: Reference[Patient]
  val period: Period[LocalDate]
  val diagnoses: Option[List[Reference[Diagnosis]]]
}


object EpisodeOfCare
{

/*
  object Status
  extends CodedEnum("dnpm-dip/episode/status")
  with DefaultCodeSystem
  {

    val Planned   = Value("planned")
    val Active    = Value("active")
    val Finished  = Value("finished")
    val Cancelled = Value("cancelled")
    val Unknown   = Value("unknown")


    override val display =
      Map(
        Planned    -> "In Vorbereitung",
        Active     -> "Laufend",
        Finished   -> "Abgeschlossen",
        Cancelled  -> "Abgebrochen",
        Unknown    -> "Unbekannt" 
      )

    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }

  }
*/


/*
  object StatusReason
  extends CodedEnum("dnpm-dip/episode/status-reason")
  with DefaultCodeSystem
  {

    val NoSeqReq = Value("no-sequencing-requested")

    override val display =
      Map(
        NoSeqReq -> "Keine Sequenzierung beantragt"
      )
    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }
  }
*/


/*
  object SubmissionType
  extends CodedEnum("dnpm-dip/episode/status")
  with DefaultCodeSystem
  {

    val Initial    = Value("initial")
    val Addition   = Value("addition")
    val Correction = Value("correction")
    val Other      = Value("other")

    override val display =
      Map(
        Initial    -> "Erstmeldung",
        Addition   -> "Nachtrag",
        Correction -> "Korrektur",
        Other      -> "Löschung",
      )
    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }
  }
*/

}
