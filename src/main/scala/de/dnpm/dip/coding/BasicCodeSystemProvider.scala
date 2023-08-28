package de.dnpm.dip.coding



import java.net.URI
import cats.data.NonEmptyList
import cats.Applicative


abstract class BasicCodeSystemProvider[S: Coding.System](
  versionOrder: Ordering[String],
  cs: CodeSystem[S],
  css: CodeSystem[S]*
)
{

  val codeSystems: Map[String,CodeSystem[S]] =
    (cs +: css).map(c => c.version.get -> c)
      .toMap
    

  final class Facade[F[_]] extends CodeSystemProvider[S,F,Applicative[F]]
  {
    self =>

    import cats.syntax.functor._
    import cats.syntax.applicative._

    override val versionOrdering = versionOrder

    override val uri: URI =
      Coding.System[S].uri

    override def versions(  
      implicit env: Applicative[F]
    ): F[NonEmptyList[String]] =
      NonEmptyList.of(cs,css: _*).map(_.version.get).pure[F]
  
    override def latestVersion(  
      implicit env: Applicative[F]
    ): F[String] =
      self.versions
        .map(_.toList.max(versionOrder))

  
    override def get(
      version: String
    )(
      implicit env: Applicative[F] 
    ): F[Option[CodeSystem[S]]] =
      codeSystems.get(version).pure[F]
  
    override def latest(  
      implicit env: Applicative[F]
    ): F[CodeSystem[S]] =
      self.latestVersion
        .map(codeSystems(_))

  }

}


// For use e.g. with Enum CodeSystem
abstract class SingleCodeSystemProvider[S: Coding.System](
  codeSystem: CodeSystem[S]
)
{

  private val version =
    codeSystem.version.getOrElse("N/A")


  final class Facade[F[_]] extends CodeSystemProvider[S,F,Applicative[F]]
  {
    self =>

    import cats.syntax.functor._
    import cats.syntax.applicative._

    override val versionOrdering =
      Version.Unordered

    override val uri: URI =
      Coding.System[S].uri


    override def versions(  
      implicit env: Applicative[F]
    ): F[NonEmptyList[String]] =
      NonEmptyList.of(version).pure[F]
  
    override def latestVersion(  
      implicit env: Applicative[F]
    ): F[String] =
      version.pure[F]

  
    override def get(
      version: String
    )(
      implicit env: Applicative[F] 
    ): F[Option[CodeSystem[S]]] =
      Option(codeSystem).pure[F]
  
    override def latest(  
      implicit env: Applicative[F]
    ): F[CodeSystem[S]] =
      codeSystem.pure[F]

  }

}
