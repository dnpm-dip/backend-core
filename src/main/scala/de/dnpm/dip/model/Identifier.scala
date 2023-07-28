package de.dnpm.dip.model


import java.net.URI
import java.util.UUID
import play.api.libs.json.Json



final case class Id[+T](value: String) extends AnyVal
{
  override def toString: String = value
}

object Id
{
  implicit def format[T] = Json.valueFormat[Id[T]]
}


final case class ExternalId
(
  value: String,
  system: Option[URI]
)

object ExternalId
{

  def apply(
    value: String,
    system: String
  ): ExternalId =
    ExternalId(
      value,
      Some(URI.create(system))
    )

  implicit val format = Json.format[ExternalId]

}
