package de.dnpm.dip.util


import cats.data.NonEmptyList
import shapeless.{
  HList,
  ::,
  HNil,
  Generic,
  Lazy
}


@annotation.implicitNotFound(
  "Couldn't find Completer[${T}] instance. Define one or ensure it is in scope."
)
trait Completer[T] extends (T => T)


object Completer
{

  object syntax 
  {

    implicit class CompleteSyntax[T](val t: T) extends AnyVal
    {
      def complete(
        implicit cp: Completer[T]
      ): T = cp(t)
    }

  }


  def of[T](f: T => T): Completer[T] =
    new Completer[T]{
      override def apply(t: T) = f(t)
    }


  import scala.language.implicitConversions

  implicit def fromFunction[T](f: T => T): Completer[T] =
    Completer.of(f)


  import scala.collection.Factory
  import syntax._

  implicit def optionCompleter[T: Completer]: Completer[Option[T]] =
    opt => opt.map(_.complete)

  implicit def iterableCompleter[
    T: Completer,
    C[X] <: Iterable[X]
  ](
    implicit fac: Factory[T,C[T]]
  ): Completer[C[T]] =
    ts => ts.map(_.complete).to(fac)

  implicit def nonEmptyListCompleter[T: Completer]: Completer[NonEmptyList[T]] =
    ts => ts.map(_.complete)


  def apply[T](implicit cp: Completer[T]) = cp

}
