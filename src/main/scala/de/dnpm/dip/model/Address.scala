package de.dnpm.dip.model


import play.api.libs.json.{
  Json,
  Format,
  OFormat
}


final case class Address
(
  municipalityCode: Address.MunicipalityCode
)

object Address
{

  final case class MunicipalityCode(value: String) extends AnyVal {
    override def toString = value
  }

  implicit val formatMunicipalityCode: Format[MunicipalityCode] =
    Json.valueFormat[MunicipalityCode]

  implicit val format: OFormat[Address] =
    Json.format[Address]
}
