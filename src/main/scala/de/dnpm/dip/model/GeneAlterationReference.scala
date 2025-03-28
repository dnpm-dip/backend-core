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
 * clinically relevant gene in a variant supporting a recommendation
 *
 * Gene is kept optional because in SNVs it is already implicitly defined,
 * and thus needn't be specified again, but for CNVs or Fusions, which can
 * affect multiple or 2 genes, respectively, it should be specified to remove ambiguity
 *
 */

final case class GeneAlterationReference[+T <: BaseVariant]
(
//  variant: InternalReference[T],
  variant: Reference[T],
  gene: Option[Coding[HGNC]], 
  display: Option[String] = None
)
{

  def resolveOn[TT >: T <: { def id: Id[_] }](
    ts: { def find(f: TT => Boolean): Option[TT] }
  ): Option[TT] =
    Reference.Resolver.onCollection(ts)(variant)

  def withDisplay(d: String): GeneAlterationReference[T] =
    this.copy(display = Some(d))

}

object GeneAlterationReference
{

  def to[T <: BaseVariant { def id: Id[T] }](
    variant: T,
    gene: Option[Coding[HGNC]] = None
  ): GeneAlterationReference[T] =
    GeneAlterationReference(
      Reference.to(variant),
      gene
    )


  implicit def reads[T <: BaseVariant]: Reads[GeneAlterationReference[T]] =
    Json.reads[GeneAlterationReference[T]]

  implicit def writes[T <: BaseVariant: Reference.TypeName]: OWrites[GeneAlterationReference[T]] =
    Json.writes[GeneAlterationReference[T]]

}

