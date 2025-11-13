package de.dnpm.dip.util


import java.time.YearMonth
import java.time.format.DateTimeFormatter
import scala.util.Try
import play.api.libs.json._
import cats.data.NonEmptyList


package object json
{

  implicit def writesNel[T: Writes](
    implicit writes: Writes[List[T]]
  ): Writes[NonEmptyList[T]] =
    writes.contramap(_.toList)


  implicit def readsNel[T: Reads](
    implicit reads: Reads[List[T]],
  ): Reads[NonEmptyList[T]] =
    reads
      .filterNot(JsonValidationError("Found empty list where non-empty list expected"))(_.isEmpty)
      .map(NonEmptyList.fromListUnsafe)


  // Custom implementation of Format[Enum] because the implementation
  // in Play JSON Lib Json.formatEnum() doesn't return the valueset in its error message
  def enumFormat[E <: Enumeration](e: E): Format[e.Value] =
    Format(
      Reads.of[String]
        .filter(
          JsonValidationError(s"Invalid enum value, expected one of {${e.values.mkString(",")}}")
        )(
          s => e.values.exists(_.toString == s)
        )
        .map(e.withName),
      Json.formatEnum(e)
    )


  private val yyyyMM = DateTimeFormatter.ofPattern("yyyy-MM")

  implicit val readsYearMonth: Reads[YearMonth] =
    Reads.of[String].flatMapResult(
      s =>
        Try {
          YearMonth.parse(s,yyyyMM)
        }
        .fold(
          _ => JsError(Seq(JsPath -> Seq(JsonValidationError("error.expected.yearmonth.isoformat")))),
          JsSuccess(_)
        )
    )

  implicit val writesYearMonth: Writes[YearMonth] =
    Writes.of[String].contramap(yyyyMM.format)

}
