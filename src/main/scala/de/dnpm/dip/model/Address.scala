package de.dnpm.dip.model


import play.api.libs.json.{
  Json,
  OFormat
}


final case class Address
(
  municipalityCode: String
)

object Address
{
  implicit val formal: OFormat[Address] =
    Json.format[Address]
}
