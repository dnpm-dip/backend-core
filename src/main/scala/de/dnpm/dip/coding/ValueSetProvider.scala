package de.dnpm.dip.coding


import java.net.URI
import cats.data.NonEmptyList
import cats.Applicative
import cats.Eval
import play.api.libs.json.{
  Json,
  OWrites
}
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}

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


trait ValueSetProviderSPI extends SPIF[
  ({ type Service[F[_]] = ValueSetProvider[Any,F,Applicative[F]] })#Service
]

object ValueSetProvider extends SPILoaderF[ValueSetProviderSPI]
{

  final case class Info
  (
    name: String,
    title: Option[String],
    uri: URI,
    versions: List[String],
    latestVersion: String
  )

  implicit val format: OWrites[Info] =
    Json.writes[Info]


  import scala.language.implicitConversions

  implicit def toAnyValueSetProvider[S,Spr >: S,F[_],Env](
    vsp: ValueSetProvider[S,F,Env]
  ): ValueSetProvider[Spr,F,Env] =
    vsp.asInstanceOf[ValueSetProvider[Spr,F,Env]]

}

/*
abstract class BasicValueSetProvider[S](
  uri: URI,
  versionOrder: Ordering[String],
  vs: ValueSet[S],
  vss: ValueSet[S]*
)
{

  val valueSets: Map[String,ValueSet[S]] =
    (vs +: vss).map(v => v.version.get -> v)
      .toMap


  final class Facade[F[_]] extends ValueSetProvider[S,F,Applicative[F]]
  {
    self =>

    import cats.syntax.functor._
    import cats.syntax.applicative._

    override val versionOrdering = versionOrder

    override val uri: URI =
      uri

    override def versions(
      implicit env: Applicative[F]
    ): F[NonEmptyList[String]] =
      NonEmptyList.of(cs,css: _*).map(_.version.get).pure[F]

    override def latestVersion(
      implicit env: Applicative[F]
    ): F[String] =
      self.versions
        .map(_.toList.max(versionOrder))

    override def filters(
      implicit env: Applicative[F]
    ): F[List[ValueSet.Filter[S]]] =
      List.empty.pure


    override def get(
      version: String
    )(
      implicit env: Applicative[F]
    ): F[Option[ValueSet[S]]] =
      codeSystems.get(version).pure[F]

    override def latest(
      implicit env: Applicative[F]
    ): F[ValueSet[S]] =
      self.latestVersion
        .map(codeSystems(_))

  }

}
*/

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

