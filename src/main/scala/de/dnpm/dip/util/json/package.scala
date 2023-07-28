package de.dnpm.dip.util



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


  implicit def formatNel[T: Reads: Writes](
    implicit
    reads: Reads[List[T]],
    writes: Writes[List[T]]
  ): Format[NonEmptyList[T]] =
    Format[NonEmptyList[T]](
      reads
        .filterNot(JsonValidationError("Found empty list where non-empty list expected"))(_.isEmpty)
        .map(NonEmptyList.fromListUnsafe),
      writes.contramap(_.toList)
    )

}
