package de.dnpm.dip.coding



import java.net.URI
import cats.data.NonEmptyList
import cats.{
  Applicative,
  Functor,
  Monad
}
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}
import play.api.libs.json.{
  Json,
  OWrites
}



trait CodeSystemProvider[S,F[_],Env]
{
  self =>

  import cats.syntax.functor._
  import cats.syntax.flatMap._

  val uri: URI

  val versionOrdering: Ordering[String]

  def versions(  
    implicit env: Env
  ): F[NonEmptyList[String]]

  def latestVersion(  
    implicit env: Env
  ): F[String]

  def filters(
    implicit env: Env
  ): F[List[CodeSystem.Filter[S]]]

  def filter(
    name: String
  )(
    implicit
    env: Env,
    func: Functor[F]
  ): F[Option[CodeSystem.Filter[S]]] =
    self.filters
      .map(_.find(_.name == name))


  def get(
    version: String
  )(
    implicit env: Env
  ): F[Option[CodeSystem[S]]]

  def latest(  
    implicit env: Env
  ): F[CodeSystem[S]]



/*
  import cats.syntax.traverse._

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

  final case class Info[S]
  (
    name: String,
    title: Option[String],
    uri: URI,
    versions: List[String],
    latestVersion: String,
    filters: List[CodeSystem.Filter[S]],
  )

  implicit def format[S]: OWrites[Info[S]] =
    Json.writes[Info[S]]


  def apply[S](implicit csp: CodeSystemProvider[S,cats.Id,Applicative[cats.Id]]) =
    csp


  import scala.language.implicitConversions

  implicit def toAnyCodeSystemProvider[S,Spr >: S,F[_],Env](
    csp: CodeSystemProvider[S,F,Env]
  ): CodeSystemProvider[Spr,F,Env] =
    csp.asInstanceOf[CodeSystemProvider[Spr,F,Env]]

}
