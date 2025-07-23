package de.dnpm.dip.model


import cats.Applicative
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  DefaultCodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI,
}


trait NGSReport extends DiagnosticReport
{
  val `type`: Coding[NGSReport.Type.Value]

  def variants: Seq[BaseVariant]
}


object NGSReport
{

  object Type
  extends CodedEnum("dnpm-dip/ngs/type")
  with MolecularDiagnostics.Type
  with DefaultCodeSystem
  {

    override val display = defaultDisplay

    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }

  }


  object Platform
  extends CodedEnum("dnpm-dip/ngs/sequencing-platform")
  with DefaultCodeSystem
  {
    val Illumina       = Value("illu")
    val ONT            = Value("ont")
    val TenXGenomics   = Value("10xg")
    val PacBio         = Value("pacb")
    val MGI            = Value("mgi")
    val UltimaGenomics = Value("ug")
    val Other          = Value("other")

    override val display =
      Map(
        Illumina       -> "Illumina",
        ONT            -> "ONT",
        TenXGenomics   -> "10X Genomics",
        PacBio         -> "PacBio",
        MGI            -> "MGI",
        UltimaGenomics -> "Ultima Genomics",
        Other          -> "Other"
      )

    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }

  }


}
