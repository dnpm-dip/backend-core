package de.dnpm.dip.util


import java.time.{
  LocalDate,
  YearMonth
}
import java.time.format.DateTimeFormatter
import scala.util.{
  Either,
  Left,
  Right,
  Try
}
import play.api.libs.json._
import cats.data.NonEmptyList


package object json
{


  implicit def writesNel[T: Writes](
    implicit
    writes: Writes[List[T]]
  ): Writes[NonEmptyList[T]] =
    writes.contramap(_.toList)


  implicit def readsNel[T: Reads](
    implicit
    reads: Reads[List[T]],
  ): Reads[NonEmptyList[T]] =
    reads
      .filterNot(JsonValidationError("Found empty list where non-empty list expected"))(_.isEmpty)
      .map(NonEmptyList.fromListUnsafe)


  // Custom implementation of Format[Enum] because the implementation
  // in Play JSON Lib Json.formatEnum() doesn't return the valueset in its error message
  def enumFormat[E <: Enumeration](
    e: E
  ): Format[E#Value] =
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


  private val yyyyMM    = "yyyy-MM"
  private val yyyyMMFormatter = DateTimeFormatter.ofPattern(yyyyMM)


  implicit val readsYearMonth: Reads[YearMonth] =
    Reads.of[String]
      .map(YearMonth.parse(_,yyyyMMFormatter))
      .orElse(
        Reads.of[LocalDate]
          .map(d => YearMonth.of(d.getYear,d.getMonth))
      )

  implicit val writesYearMonth: Writes[YearMonth] =
    Writes.of[String].contramap(yyyyMMFormatter.format)


}
