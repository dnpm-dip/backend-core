package de.dnpm.dip.coding



sealed trait Ensembl
object Ensembl
{
  implicit val codingSystem: Coding.System[Ensembl] =
    Coding.System[Ensembl]("https://www.ensembl.org")
}



sealed trait Entrez
object Entrez
{
  implicit val codingSystem: Coding.System[Entrez] =
    Coding.System[Entrez]("https://www.ncbi.nlm.nih.gov/entrez")
}



sealed trait RefSeq
object RefSeq
{

  implicit val codingSystem: Coding.System[RefSeq] =
    Coding.System[RefSeq]("https://www.ncbi.nlm.nih.gov/refseq")

  sealed trait NM
  object NM
  {
    implicit val codingSystem: Coding.System[NM] =
      Coding.System[NM]("https://www.ncbi.nlm.nih.gov/refseq/nm")
  }

  sealed trait NP
  object NP
  {
    implicit val codingSystem: Coding.System[NP] =
      Coding.System[NP]("https://www.ncbi.nlm.nih.gov/refseq/np")
  }

}

