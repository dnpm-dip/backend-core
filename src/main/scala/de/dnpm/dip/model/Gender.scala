package de.dnpm.dip.model


import java.net.URI
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


object Gender
extends CodedEnum("Gender")
with DefaultCodeSystem
{

  val Male    = Value("male")
  val Female  = Value("female")
  val Other   = Value("other")
  val Unknown = Value("unknown")

/*
  override val display = {
    case Male    => "Männlich" 
    case Female  => "Weiblich" 
    case Other   => "Divers"
    case Unknown => "Unbekannt"
  }
*/

  override val display =
    Map(
      Male    -> "Männlich",
      Female  -> "Weiblich", 
      Other   -> "Divers",
      Unknown -> "Unbekannt"
    )


  final class ProviderSPI extends CodeSystemProviderSPI
  {
    override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
      new Provider.Facade[F]
  }


  implicit val format: Format[Gender.Value] =
    Json.formatEnum(this)
}
