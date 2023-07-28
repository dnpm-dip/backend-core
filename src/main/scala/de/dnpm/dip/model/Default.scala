package de.dnpm.dip.model



final case class Default[T](value: T)

object Default
{

  def apply[T](implicit d: Default[T]) = d

  def valueOf[T](implicit d: Default[T]): T = d.value
}
