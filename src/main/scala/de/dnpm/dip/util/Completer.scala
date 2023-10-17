package de.dnpm.dip.util


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


  import scala.collection.Factory
  import syntax._

  implicit def optionCompleter[
    T: Completer
  ]: Completer[Option[T]] =
    Completer.of(
      opt => opt.map(_.complete)
    )

  implicit def iterableCompleter[
    T: Completer,
    C[X] <: Iterable[X]
  ](
    implicit fac: Factory[T,C[T]]
  ): Completer[C[T]] =
    Completer.of(
      ts => ts.map(_.complete).to(fac)
    )


  def apply[T](implicit cp: Completer[T]) = cp


/*
  object derivation
  {

    implicit def hlistCompleter[H,T <: HList](
      implicit
      hComp: Completer[H],
      tComp: Completer[T],
    ): Completer[H :: T] =
      Completer.of(
        hlist => hComp(hlist.head) :: tComp(hlist.tail)
      )

    implicit val hnilCompleter: Completer[HNil] =
      Completer.of(identity)


    implicit def genericCompleter[T,Tpr](
      implicit
      gen: Generic.Aux[T,Tpr],
      comp: Completer[Tpr]
    ): Completer[T] = {

      import scala.util.chaining._

      Completer.of(
        gen.to(_) pipe comp pipe gen.from
      )
    }
  }
*/

}
