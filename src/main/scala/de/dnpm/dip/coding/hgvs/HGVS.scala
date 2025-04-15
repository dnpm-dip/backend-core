package de.dnpm.dip.coding.hgvs


import scala.util.matching.UnanchoredRegex
import de.dnpm.dip.coding.{
  Code,
  Coding,
  CodeSystem,
}


sealed trait HGVS

object HGVS extends CodeSystem.Publisher[HGVS]
{

  implicit val codingSystem: Coding.System[HGVS] =
    Coding.System[HGVS]("https://hgvs-nomenclature.org")


  sealed trait DNA extends HGVS
  sealed trait Protein extends HGVS


  implicit val codingSystemDNA: Coding.System[HGVS.DNA] =
    Coding.System[HGVS.DNA]("https://hgvs-nomenclature.org")

  implicit val codingSystemProtein: Coding.System[HGVS.Protein] =
    Coding.System[HGVS.Protein]("https://hgvs-nomenclature.org")


  override val properties =
    List.empty

  override val filters =
    List.empty


  object extensions
  {

    implicit class HGVSCodeExtensions[C <: HGVS](val code: Code[C])
    {
      def matches(criterion: Code[C]): Boolean =
        code.value.toLowerCase contains criterion.value.toLowerCase 
    }

/*    
    implicit class HGVSCodingExtensions[C <: HGVS](val coding: Coding[C]) extends AnyVal
    {
      def matches(criterion: Coding[C]): Boolean =
        coding.code.value.toLowerCase contains criterion.code.value.toLowerCase 
    }
*/    
  }



  object Protein
  {

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


     val threeLetterCode: UnanchoredRegex =
       s"(?i)(${aminoAcidMappings.values.mkString("|")})".r.unanchored

     val oneLetterCode: UnanchoredRegex =
       s"(${(aminoAcidMappings.keySet - "*").mkString("|")}|\\*)".r.unanchored


     def to3LetterCode(in: String): String =
       in match {
         case threeLetterCode(_*) => in
         case _                 => oneLetterCode replaceAllIn (in, m => aminoAcidMappings(m.matched))
       }

     def to1LetterCode(in: String): String =
       in match {
         case threeLetterCode(_*) => threeLetterCode replaceAllIn (in, m => invertedMappings(m.matched.toLowerCase))
         case _                 => in
       }

  }

}
