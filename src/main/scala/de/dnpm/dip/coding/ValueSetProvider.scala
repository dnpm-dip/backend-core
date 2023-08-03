package de.dnpm.dip.coding


import java.net.URI
import cats.data.NonEmptyList
import cats.Applicative
import cats.Eval
/*
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}
*/

trait ValueSetProvider[S,F[_],Env]
{
  self =>

  val uri: URI

  val versionOrdering: Ordering[String]

  def versions(  
    implicit env: Env
  ): F[NonEmptyList[String]]

  def latestVersion(  
    implicit env: Env
  ): F[String]

  def get(
    version: String
  )(
    implicit env: Env
  ): F[Option[ValueSet[S]]]

  def latest(
    implicit env: Env
  ): F[ValueSet[S]]

}


class BasicValueSetProvider[S,F[_]](
  private val versionOrder: Ordering[String],
  private val vss: NonEmptyList[ValueSet[S]]
)
extends ValueSetProvider[S,F,Applicative[F]]
{
  self =>

  private val valueSets: Map[String,ValueSet[S]] =
    vss.map(c => c.version.get -> c)
      .toList
      .toMap


  import cats.syntax.functor._
  import cats.syntax.applicative._

  override val versionOrdering =
    versionOrder

  override val uri: URI =
    vss.head.uri


  override def versions(
    implicit env: Applicative[F]
  ): F[NonEmptyList[String]] =
    vss.map(_.version.get)
      .pure[F]

  override def latestVersion(
    implicit env: Applicative[F]
  ): F[String] =
    self.versions
      .map(_.toList.max(versionOrder))


  override def get(
    version: String
  )(
    implicit env: Applicative[F]
  ): F[Option[ValueSet[S]]] =
    valueSets.get(version)
      .pure[F]

  override def latest(
    implicit env: Applicative[F]
  ): F[ValueSet[S]] =
    self.latestVersion
      .map(valueSets(_))

}


class LazyValueSetProvider[S,F[_]](
  private val csp: CodeSystemProvider[S,cats.Id,Applicative[cats.Id]],
  private val composer: ValueSet.Composer
)
extends ValueSetProvider[Any,F,Applicative[F]]
{
  self =>

  import cats.syntax.functor._
  import cats.syntax.applicative._

  private val valueSets: Map[String,Eval[ValueSet[Any]]] =
    csp.versions.map(
      v => v -> Eval.later(composer.expand(csp.get(v).get))
    )
    .toList
    .toMap


  override val versionOrdering =
    csp.versionOrdering

  override val uri: URI =
    csp.uri


  override def versions(
    implicit env: Applicative[F]
  ): F[NonEmptyList[String]] =
    csp.versions.pure

  override def latestVersion(
    implicit env: Applicative[F]
  ): F[String] =
    csp.latestVersion.pure


  override def get(
    version: String
  )(
    implicit env: Applicative[F]
  ): F[Option[ValueSet[Any]]] =
    valueSets.get(version)
      .map(_.value)
      .pure

  override def latest(
    implicit env: Applicative[F]
  ): F[ValueSet[Any]] =
    self.latestVersion
      .map(valueSets(_).value)

}

/*
trait ValueSetProviderSPI extends SPIF[
  ({ type Service[F[_]] = ValueSetProvider[Any,F,Applicative[F]] })#Service
]

object ValueSetProvider extends SPILoaderF[ValueSetProviderSPI]
*/
