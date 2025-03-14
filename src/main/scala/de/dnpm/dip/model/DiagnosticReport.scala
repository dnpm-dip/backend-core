package de.dnpm.dip.model


import java.time.LocalDate
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem
}


trait DiagnosticReport extends Commentable
{
  val id: Id[DiagnosticReport]
  val patient: Reference[Patient]
  val issuedOn: LocalDate
  val `type`: Coding[_]
}



object MolecularDiagnostics
{

  trait Type
  {
    self: CodedEnum with DefaultCodeSystem =>

    val Array           = Value("array")
    val Single          = Value("single")
    val Karyotyping     = Value("karyotyping")
    val Panel           = Value("panel")
    val Exome           = Value("exome")
    val GenomeShortRead = Value("genome-short-read")
    val GenomeLongRead  = Value("genome-long-read")
    val Other           = Value("other")

    protected val defaultDisplay =
      Map(
        Array           -> "Array",
        Single          -> "Single",
        Karyotyping     -> "Karyotyping",
        Panel           -> "Panel",
        Exome           -> "Exome",
        GenomeShortRead -> "Genome short-read",
        GenomeLongRead  -> "Genome long-read",
        Other           -> "Other"
      )

  }

}
