package de.dnpm.dip.model


import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem,
}
import play.api.libs.json.{
  Json,
  Format
}



trait BaseVariant
{
  val id: Id[BaseVariant]
  val externalIds: Option[List[ExternalId[BaseVariant,_]]]
  val patient: Reference[Patient]
  val localization: Option[Set[Coding[BaseVariant.Localization.Value]]]
}

object BaseVariant
{

  object Localization
  extends CodedEnum("dnpm-dip/variant/localization")
  with DefaultCodeSystem
  {
    val CodingRegion     = Value("coding-region")
    val SplicingRegion   = Value("splicing-region")
    val RegulatoryRegion = Value("regulatory-region")
    val Intronic         = Value("intronic")
    val Intergenic       = Value("intergenic")

    override val display =
      Map(
        CodingRegion     -> "Coding region",
        SplicingRegion   -> "splicing region",
        RegulatoryRegion -> "Regulatory region",
        Intronic         -> "Intronic",
        Intergenic       -> "Intergenic"
      )

  }

}


object Chromosome extends Enumeration
{
  val chr1,
      chr2,
      chr3,
      chr4,
      chr5,
      chr6,
      chr7,
      chr8,
      chr9,
      chr10,
      chr11,
      chr12,
      chr13,
      chr14,
      chr15,
      chr16,
      chr17,
      chr18,
      chr19,
      chr20,
      chr21,
      chr22,
      chrX,
      chrY,
      chrMT = Value

  implicit val format: Format[Value] =
    Json.formatEnum(this)
}

/*
trait Chromosome
{
  this: Enumeration => 

  val chr1,
      chr2,
      chr3,
      chr4,
      chr5,
      chr6,
      chr7,
      chr8,
      chr9,
      chr10,
      chr11,
      chr12,
      chr13,
      chr14,
      chr15,
      chr16,
      chr17,
      chr18,
      chr19,
      chr20,
      chr21,
      chr22,
      chrX,
      chrY = Value
}
*/
