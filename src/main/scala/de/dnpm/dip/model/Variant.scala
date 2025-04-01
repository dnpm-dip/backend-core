package de.dnpm.dip.model


import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem,
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
