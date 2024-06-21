package de.dnpm.dip.util.mapping


import scala.collection.Factory
import cats.data.NonEmptyList


object syntax
{

  implicit class MappingOps[A](val a: A) extends AnyVal
  {
    def mapTo[B](implicit f: A => B): B = f(a)
  }


  implicit class IterableMappingOps[C[X] <: IterableOnce[X], A](val as: C[A])  extends AnyVal
  {

    def mapAllTo[B](
      implicit
      f: A => B,
      fac: Factory[B,C[B]]
    ): C[B] =
      as.iterator.map(f).to(fac)

  }

  implicit class NonEmptyListOps[A](val as: NonEmptyList[A])  extends AnyVal
  {

    def mapAllTo[B](
      implicit f: A => B
    ): NonEmptyList[B] =
      as.map(f)

  }

}
