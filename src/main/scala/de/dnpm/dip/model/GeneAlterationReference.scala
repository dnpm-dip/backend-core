package de.dnpm.dip.model


import de.dnpm.dip.coding.Coding
import de.dnpm.dip.coding.hgnc.HGNC
import play.api.libs.json.{
  Json,
  Reads,
  OWrites
}

/*
 * Conceptually equivalent to FHIR "CodeableReference", to specify the
 * (clinically) relevant gene in a variant supporting a recommendation
 *
 * Gene is kept optional because in SNVs it is already implicitly defined,
 * and thus needn't be specified again, but for CNVs or Fusions, which can
 * affect multiple or 2 genes, respectively,
 * it should be specified to remove ambiguity
 *
 */

final case class GeneAlterationReference[+T]
(
  gene: Option[Coding[HGNC]], 
  variant: Reference[T],
  display: Option[String]
)
{

  def resolve[TT >: T](
    implicit resolver: Reference.Resolver[TT]
  ): Option[TT] =
    resolver(variant)

  def resolveOn[TT >: T <: { def id: Id[_] }](
    ts: Iterable[TT]
  ): Option[TT] =
    Reference.Resolver.on(ts)(variant)

  def withDisplay(d: String): GeneAlterationReference[T] =
    this.copy(display = Some(d))

}


object GeneAlterationReference
{

  def apply[T <: { def id: Id[T] }](
    gene: Option[Coding[HGNC]],
    variant: T
  ): GeneAlterationReference[T] =
    GeneAlterationReference(
      gene,
      Reference.to(variant),
      None
    )


  implicit def reads[T]: Reads[GeneAlterationReference[T]] =
    Json.reads[GeneAlterationReference[T]]
      // For (temporary) backward compatibility, fall back to parsing as a normal reference
      //.orElse(
      //  Reads.of[Reference[T]].map(GeneAlterationReference(None,_,None))
      //)

  implicit def writes[T: Reference.TypeName]: OWrites[GeneAlterationReference[T]] =
    Json.writes[GeneAlterationReference[T]]

}
