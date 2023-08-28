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


final case class ExternalId[+T]
(
  value: String,
  system: Option[URI]
)

object ExternalId
{

  def apply[T](
    value: String,
    system: String
  ): ExternalId[T] =
    ExternalId(
      value,
      Some(URI.create(system))
    )

  implicit def format[T] = Json.format[ExternalId[T]]

}
