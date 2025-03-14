package de.dnpm.dip.model



final case class Default[T](value: T)


object Default extends Enumeration
{

  val Unknown = Value("unknown")


  def valueOf[T](implicit d: Default[T]): T = d.value
}



