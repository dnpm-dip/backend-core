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


  implicit def writesNel[T: Writes](
    implicit
    reads: Reads[List[T]],
    writes: Writes[List[T]]
  ): Writes[NonEmptyList[T]] =
    writes.contramap(_.toList)


  implicit def readsNel[T: Reads](
    implicit
    reads: Reads[List[T]],
    writes: Writes[List[T]]
  ): Reads[NonEmptyList[T]] =
    reads
      .filterNot(JsonValidationError("Found empty list where non-empty list expected"))(_.isEmpty)
      .map(NonEmptyList.fromListUnsafe)

}
