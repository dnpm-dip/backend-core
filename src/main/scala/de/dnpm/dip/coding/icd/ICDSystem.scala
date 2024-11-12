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
          .collect {
            case c if c.classKind == kind => c.toCoding
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

}
