package de.dnpm.dip.coding.icd


import cats.Applicative
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

trait ICDSystem[T <: ICD] extends CodeSystem.Publisher[T]
{

  val ClassKind =
    CodeSystem.Property(
      ClassKinds,
      "kind",
      Some("Kind of ICD class")
    )

  override val properties =
    List(
      ClassKind
    )


  val filterByClassKind: Map[ClassKinds.Value,CodeSystem.Filter[T]] =
    ClassKinds.values
      .toList
      .map { kind =>

        import extensions._

        kind -> CodeSystem.Filter[T](
          s"is-a-$kind",
          Some(s"Filter ICD classes of kind '$kind'"),
          _.classKind == kind
        )
      }
      .toMap


  override val filters =
    filterByClassKind.values.toList


  object extensions 
  {

    implicit class ICDConceptProperties[Tpr <: T](
      val c: CodeSystem.Concept[Tpr]
    )
    {

      def classKind: ClassKinds.Value =
        ClassKinds.withName(c.get(ClassKind).get.head)  // safe, always defined
    }

  }

}
