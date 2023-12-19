package de.dnpm.dip.coding



import java.net.URI
import cats.data.NonEmptyList
import cats.{
  Applicative,
  Monad
}
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}


trait CodeSystemProvider[S,F[_],Env]
{
  self =>

  import cats.syntax.functor._
  import cats.syntax.flatMap._
  import cats.syntax.traverse._

  val uri: URI

  val versionOrdering: Ordering[String]

  def versions(  
    implicit env: Env
  ): F[NonEmptyList[String]]

  def latestVersion(  
    implicit env: Env
  ): F[String]

/*  
  def filters(
    implicit env: Env
  ): F[List[CodeSystem.Filter[S]]]
*/

  def get(
    version: String
  )(
    implicit env: Env
  ): F[Option[CodeSystem[S]]]

  def latest(  
    implicit env: Env
  ): F[CodeSystem[S]]



/*
  def findConcept(
    code: Code[S],
    version: Option[String] = None
  )(
    implicit
    env: Env,
    monad: Monad[F]
  ): F[Option[CodeSystem.Concept[S]]] = 
    findConceptBy(
      _.concept(code),
      version
    )

  def findConceptWithDisplay(
    display: String,
    version: Option[String] = None
  )(
    implicit
    env: Env,
    monad: Monad[F]
  ): F[Option[CodeSystem.Concept[S]]] = 
    findConceptBy(
      _.concepts.find(_.display == display),
      version
    )


  private def findConceptBy(
    f: CodeSystem[S] => Option[CodeSystem.Concept[S]],
    version: Option[String] = None
  )(
    implicit
    env: Env,
    monad: Monad[F]
  ): F[Option[CodeSystem.Concept[S]]] =
    version match {
      case Some(v) =>
        self.get(v)
          .map(
            _.flatMap(f)
          )

      case None =>
        versions
          .map(
            // Sort the versions in decreasing order, i.e. with latest in front
            _.toList
             .sorted(versionOrdering.reverse)
           )
           .flatMap(
             // Traverse the CodeSystems to find the concept with given code
             _.traverse(
               v =>
                 get(v)
                   .map(
                     _.flatMap(f)
                   )
             )
           )
           .map(
             _.collectFirst {
               case Some(concept) => concept
             }
           )
    }
*/
}

trait CodeSystemProviderSPI extends SPIF[
  ({ type Service[F[_]] = CodeSystemProvider[Any,F,Applicative[F]] })#Service
]

object CodeSystemProvider extends SPILoaderF[CodeSystemProviderSPI]
{

  def apply[S](implicit csp: CodeSystemProvider[S,cats.Id,Applicative[cats.Id]]) =
    csp


  import scala.language.implicitConversions

  implicit def toAnyCodeSystemProvider[S,Spr >: S,F[_],Env](
    csp: CodeSystemProvider[S,F,Env]
  ): CodeSystemProvider[Spr,F,Env] =
    csp.asInstanceOf[CodeSystemProvider[Spr,F,Env]]

}
