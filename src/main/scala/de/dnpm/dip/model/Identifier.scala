package de.dnpm.dip.model


import java.net.URI
import java.util.UUID
import play.api.libs.json.{
  Json,
  Format,
  OFormat
}
import de.dnpm.dip.coding.Coding


final case class Id[+T](value: String)
{

  import scala.language.reflectiveCalls

  def resolveOn[TT >: T](
    ts: Iterable[TT]
  )(
    implicit hasId: TT <:< { def id: Id[TT] }
  ): Option[TT] =
    ts.find(_.id == this)
  

  override def toString: String = value
}

object Id
{
  implicit def format[T]: Format[Id[T]] =
    Json.valueFormat[Id[T]]
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

  def apply[T, S: Coding.System](
    value: String
  ): ExternalId[T] =
    ExternalId(
      value,
      Some(Coding.System[S].uri)
    )


  implicit def format[T]: OFormat[ExternalId[T]] =
    Json.format[ExternalId[T]]

}
