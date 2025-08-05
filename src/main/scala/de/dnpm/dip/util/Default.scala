package de.dnpm.dip.util



final case class Default[T] private (
  value: T
)

object Default
{
  def apply[T](implicit dflt: Default[T]) = dflt

  def valueOf[T](implicit dflt: Default[T]) = dflt.value
}
