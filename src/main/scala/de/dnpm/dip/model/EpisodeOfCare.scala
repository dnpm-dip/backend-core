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

}
