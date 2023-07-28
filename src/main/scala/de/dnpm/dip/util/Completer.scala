package de.dnpm.dip.util


@annotation.implicitNotFound(
  "Couldn't find Completer[${T}] instance. Define one or ensure it is in scope."
)
trait Completer[T] extends (T => T)


object Completer
{

  def apply[T](implicit cp: Completer[T]) = cp

  def of[T](f: T => T): Completer[T] =
    new Completer[T]{
      override def apply(t: T) = f(t)
    }


  object syntax 
  {

    implicit class CompleteSyntax[T](val t: T) extends AnyVal
    {
      def complete(
        implicit cp: Completer[T]
      ): T = cp(t)
    }

  }



}


