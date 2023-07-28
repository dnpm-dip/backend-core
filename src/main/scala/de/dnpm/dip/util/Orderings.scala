package de.dnpm.dip.util


import shapeless.{
  HList, ::, HNil, Generic
}


/*
  Utility to derive Ordering[T] instances,

  e.g. for value types:
  final case class MyValueType(value: Int) extends AnyVal

  or other product types
*/
object Orderings
{

  def deriveFor[T](
    implicit ord: Ordering[T]
  ): Ordering[T] = ord
  

  implicit def genericOrdering[T,Tpr](
    implicit
    gen: Generic.Aux[T,Tpr],
    ord: Ordering[Tpr]
  ): Ordering[T] =
    ord.on(gen.to)


  implicit def monoHListOrdering[H](
    implicit ord: Ordering[H]
  ): Ordering[H :: HNil] =
    ord.on(_.head)


  // Adapted from TupleN-Ordering in Scala Standard Library
  private[this] final class HListOrdering[H,T <: HList](
    private val headOrd: Ordering[H],
    private val tailOrd: Ordering[T]
  )
  extends Ordering[H :: T]
  {

    override def compare(
      x: H :: T,
      y: H :: T
    ): Int = {

      headOrd.compare(x.head,y.head) match {
       case 0 => tailOrd.compare(x.tail,y.tail)
       case r => r
      }
    }

    override def equals(obj: Any): Boolean =
      obj match {
        case that: AnyRef if this eq that => true
        case that: HListOrdering[_,_] =>
          this.headOrd == that.headOrd &&
          this.tailOrd == that.tailOrd
      }

    override def hashCode(): Int = 
      (headOrd :: tailOrd :: HNil).hashCode

  }


  implicit def hlistOrdering[H, T <: HList](
    implicit
    headOrd: Ordering[H],
    tailOrd: Ordering[T]
  ): Ordering[H :: T] =
    new HListOrdering[H,T](headOrd,tailOrd)

}
