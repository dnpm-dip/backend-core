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


trait Episode
{
  val id: Id[Episode]
  val transferTan: Option[Id[TransferTAN]]  //TODO: Make required 
  val patient: Reference[Patient]
//  val status: Coding[Episode.Status.Value]
  val diagnoses: List[Reference[Diagnosis]]
}


object Episode
{

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


}
