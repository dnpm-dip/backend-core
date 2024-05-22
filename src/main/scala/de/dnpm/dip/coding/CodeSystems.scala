package de.dnpm.dip.coding


import java.net.URI
import cats.{
  Applicative,
  Id
}
import shapeless.{
  Coproduct,
  :+:,
  CNil,
  Generic
}


@annotation.implicitNotFound(
"Couldn't resolve implicit CodeSystems. Ensure implicit CodeSystems are in scope for all types in ${CS}."
)
trait CodeSystems[CS <: Coproduct]{
  val values: Map[URI,CodeSystem[Any]]
}

object CodeSystems
{

  def apply[CS <: Coproduct](implicit cs: CodeSystems[CS]) = cs

  implicit def coproductCodeSystems[H, T <: Coproduct](
    implicit
    hcs: CodeSystem[H],
    tcs: CodeSystems[T]
  ): CodeSystems[H :+: T] =
    new CodeSystems[H :+: T]{
      val values = tcs.values + (hcs.uri -> hcs)
    }

  implicit val cnilCodeSystems: CodeSystems[CNil] =
    new CodeSystems[CNil]{
      val values = Map.empty
    }

}


@annotation.implicitNotFound(
"Couldn't resolve implicit CodeSystemProviders. Ensure implicit CodeSystemProviders are in scope for all types in ${CS}."
)
trait CodeSystemProviders[CS <: Coproduct]{
  val values: Map[URI,CodeSystemProvider[Any,Id,Applicative[Id]]]
}

object CodeSystemProviders
{

  def apply[CS <: Coproduct](implicit cs: CodeSystemProviders[CS]) = cs

  implicit def coproductCodeSystemProviders[H, T <: Coproduct](
    implicit
    hcs: CodeSystemProvider[H,Id,Applicative[Id]],
    tcs: CodeSystemProviders[T]
  ): CodeSystemProviders[H :+: T] =
    new CodeSystemProviders[H :+: T]{
      val values = tcs.values + (hcs.uri -> hcs)
    }

  implicit val cnilCodeSystemProviders: CodeSystemProviders[CNil] =
    new CodeSystemProviders[CNil]{
      val values = Map.empty
    }

}

