package de.dnpm.dip.coding.icd


import cats.{
  Applicative,
  Id
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodeSystemProvider
}



object ClassKinds extends Enumeration
{
  val Chapter  = Value("chapter")
  val Block    = Value("block")
  val Category = Value("category")
}


private [icd] trait ICD

object ICD
{

  val ClassKind =
    CodeSystem.Property(
      ClassKinds,
      "kind",
      Some("Kind of ICD class")
    )

  object extensions 
  {
  
    implicit class ICDConceptProperties[T <: ICD](val concept: CodeSystem.Concept[T])
    {
      def classKind: ClassKinds.Value =
        ClassKinds.withName(concept.get(ClassKind).get.head)  // safe, always defined
    }

    implicit class ICDCodingProperties[T <: ICD](val coding: Coding[T])
    {
      def parentOfKind(kind: ClassKinds.Value)(
        implicit
        csp: CodeSystemProvider[T,Id,Applicative[Id]],
        sys: Coding.System[T]
      ): Option[Coding[T]] =
        csp.get(coding.version.getOrElse(csp.latestVersion))
          .flatMap(_.parentOf(coding.code))
          .flatMap {
            case c if c.classKind == kind => Some(c.toCoding)
            case _                        => None
          }

    }
  }

}


trait ICDSystem[T <: ICD] extends CodeSystem.Publisher[T]
{

  override val properties =
    List(
      ICD.ClassKind
    )

  val filterByClassKind: Map[ClassKinds.Value,CodeSystem.Filter[T]] =
    ClassKinds.values
      .toList
      .map { kind =>

        import ICD.extensions._

        kind -> CodeSystem.Filter[T](
          s"is-a-$kind",
          Some(s"Filter ICD classes of kind '$kind'"),
          _.classKind == kind
        )
      }
      .toMap


  override def filters =
    filterByClassKind.values.toList

/*
  object extensions 
  {

    implicit class ICDConceptProperties[Tpr <: T](val concept: CodeSystem.Concept[Tpr])
    {
      def classKind: ClassKinds.Value =
        ClassKinds.withName(concept.get(ClassKind).get.head)  // safe, always defined
    }

    implicit class ICDCodingProperties[Tpr <: T](val coding: Coding[Tpr])
    {
      def parentOfKind(kind: ClassKinds.Value)(
        implicit
        csp: CodeSystemProvider[Tpr,Id,Applicative[Id]],
        sys: Coding.System[Tpr]
      ): Option[Coding[T]] =
        csp.get(coding.version.getOrElse(csp.latestVersion))
          .flatMap(_.parentOf(coding.code))
          .flatMap {
            case c if c.classKind == kind => Some(c.toCoding)
            case _                        => None
          }

    }


//    implicit class ICDConceptProperties(val concept: CodeSystem.Concept[T])
//    {
//      def classKind: ClassKinds.Value =
//        ClassKinds.withName(concept.get(ClassKind).get.head)  // safe, always defined
//    }
//
//    implicit class ICDCodingProperties(val coding: Coding[T])
//    {
//
//      def parentOfKind(kind: ClassKinds.Value)(
//        implicit csp: CodeSystemProvider[T,Id,Applicative[Id]]
//      ): Option[Coding[T]] =
//        csp.get(coding.version.getOrElse(csp.latestVersion))
//          .flatMap(_.parentOf(coding.code))
//          .flatMap {
//            case c if c.classKind == kind => Some(c.toCoding)
//            case _                        => None
//          }
//
//    }

  }
*/
}
