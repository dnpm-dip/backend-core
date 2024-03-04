package de.dnpm.dip.model


import de.dnpm.dip.coding.Coding


sealed trait Publication


sealed trait PubMed
object PubMed
{
  implicit val system: Coding.System[PubMed] =
    Coding.System[PubMed]("https://pubmed.ncbi.nlm.nih.gov/")
}

