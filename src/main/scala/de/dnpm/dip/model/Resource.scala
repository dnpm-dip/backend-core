package de.dnpm.dip.model


import java.time.Instant


trait Resource[T]
{
  this: Product =>

  val id: Id[T]

  val extIds: Set[ExternalId[T,_]]

  val lastUpdate: Instant
}

