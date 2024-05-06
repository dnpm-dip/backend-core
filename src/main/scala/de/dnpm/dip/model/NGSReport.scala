package de.dnpm.dip.model


import java.time.LocalDate
import cats.Applicative
import de.dnpm.dip.coding.{
  Coding,
  CodedEnum,
  CodeSystem,
  DefaultCodeSystem,
  CodeSystemProvider,
  CodeSystemProviderSPI,
  SingleCodeSystemProvider
}


trait NGSReport
{
  val id: Id[NGSReport]
  val patient: Reference[Patient]
  val issuedOn: LocalDate
  val sequencingType: Coding[NGSReport.SequencingType.Value]
}

//TODO: Add CodeSystemProviders to META-INF/services

object NGSReport
{

  sealed trait SequencingType
  object SequencingType
  extends CodedEnum("dnpm-dip/ngs/sequencing-type")
  with DefaultCodeSystem
  {
    val Panel           = Value("panel")
    val Array           = Value("array")
    val Exome           = Value("exome")
    val GenomeShortRead = Value("genome-short-read")
    val GenomeLongRead  = Value("genome-long-read")

    override val display =
      Map(
        Panel           -> "Panel",
        Array           -> "Array",
        Exome           -> "Exome",
        GenomeShortRead -> "Genome short-read",
        GenomeLongRead  -> "Genome long-read"
      )

    final class ProviderSPI extends CodeSystemProviderSPI
    {
      override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
        new Provider.Facade[F]
    }

  }


  sealed trait Platform
  object Platform
  extends CodedEnum("/dnpm-dip/ngs/sequencing-platform")
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
