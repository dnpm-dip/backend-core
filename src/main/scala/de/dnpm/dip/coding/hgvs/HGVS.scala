package de.dnpm.dip.coding.hgvs


import cats.Applicative
import de.dnpm.dip.util.{
  SPIF,
  SPILoaderF
}
import de.dnpm.dip.coding.{
  Coding,
  Code,
  CodeSystem,
  CodeSystemProvider
}


sealed trait HGVS

object HGVS extends CodeSystem.Publisher[HGVS]
{

  implicit val codingSystem: Coding.System[HGVS] =
    Coding.System[HGVS]("https://varnomen.hgvs.org")

  sealed trait DNA extends HGVS
  sealed trait Protein extends HGVS


  implicit val codingSystemDNA: Coding.System[HGVS.DNA] =
    Coding.System[HGVS.DNA]("https://varnomen.hgvs.org/recommendations/DNA/")

  implicit val codingSystemProtein: Coding.System[HGVS.Protein] =
    Coding.System[HGVS.Protein]("https://varnomen.hgvs.org/recommendations/protein/")


  override val properties =
    List.empty

  override val filters =
    List.empty



  object Protein
  {

    import scala.util.chaining._
    import scala.util.matching.Regex

    private val aminoAcidMappings =
      Map(
       "A"  -> "Ala",
       "B"  -> "Asx",
       "C"  -> "Cys",
       "D"  -> "Asp",
       "E"  -> "Glu",
       "F"  -> "Phe",
       "G"  -> "Gly",
       "H"  -> "His",
       "I"  -> "Ile",
       "K"  -> "Lys",
       "L"  -> "Leu",
       "M"  -> "Met",
       "N"  -> "Asn",
       "P"  -> "Pro",
       "Q"  -> "Gln",
       "R"  -> "Arg",
       "S"  -> "Ser",
       "T"  -> "Thr",
       "U"  -> "Sec",
       "V"  -> "Val",
       "W"  -> "Trp",
       "X"  -> "Xaa",
       "Y"  -> "Tyr",
       "Z"  -> "Glx",
       "*"  -> "Ter"
     )


     private val invertedMappings =
       aminoAcidMappings
         .map { case (one,three) => three.toLowerCase -> one }

     //TODO: Case insensitivity

     private val threeLetterAA =
       s"(?i)(${aminoAcidMappings.values.mkString("|")})".r.unanchored

     private val oneLetterAA =
       s"(${(aminoAcidMappings.keySet - "*").mkString("|")}|\\*)".r.unanchored


     def to3LetterCode(in: String): String =
       in match {
         case threeLetterAA(_*) => in
         case _                 => oneLetterAA replaceAllIn (in, m => aminoAcidMappings(m.matched))
       }

     def to1LetterCode(in: String): String =
       in match {
         case threeLetterAA(_*) => threeLetterAA replaceAllIn (in, m => invertedMappings(m.matched.toLowerCase))
         case _                 => in
       }

  }

}
