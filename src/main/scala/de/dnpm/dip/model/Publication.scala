package de.dnpm.dip.model


import de.dnpm.dip.coding.Coding
import shapeless.{ 
  :+:,
  CNil
}


sealed trait Publication


sealed trait PubMed
object PubMed
{
  implicit val system: Coding.System[PubMed] =
    Coding.System[PubMed]("https://pubmed.ncbi.nlm.nih.gov")
}

sealed trait DOI
object DOI
{
  implicit val system: Coding.System[DOI] =
    Coding.System[DOI]("https://www.doi.org")
}


object Publication
{
  type Systems = PubMed :+: DOI :+: CNil
}
