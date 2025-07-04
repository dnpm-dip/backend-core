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

}


sealed trait SequenceOntology
object SequenceOntology
{
  implicit val codingSystem: Coding.System[SequenceOntology] =
    Coding.System("http://www.sequenceontology.org")
}


