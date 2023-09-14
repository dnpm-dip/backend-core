package de.dnpm.dip.model


import cats.Applicative
import de.dnpm.dip.coding.{
  CodedEnum,
  DefaultCodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI
}
import play.api.libs.json.{
  Json,
  Format
}



object VitalStatus 
extends CodedEnum("dnpm-dip/patient/vital-status")
with DefaultCodeSystem
{
  val Alive    = Value("alive")
  val Deceased = Value("deceased")

  override val display = {
    case Alive    => "Lebend" 
    case Deceased => "Verstorben"
  }

  final class ProviderSPI extends CodeSystemProviderSPI
  {
    override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
      new Provider.Facade[F]
  }

  implicit val format: Format[VitalStatus.Value] =
    Json.formatEnum(this)
}
