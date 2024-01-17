package de.dnpm.dip.coding.hgvs


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.Inspectors._



class HGVSTests extends AnyFlatSpec
{

  import scala.util.chaining._

  // Source: https://varnomen.hgvs.org/recommendations/protein/variant/
  val proteinChanges =
    Seq(
      "p.Trp24Cys",
      "p.Met1_Leu2insArgSerThrVal",
      "p.(Gly56Ala^Ser^Cys)",
      "p.Trp24=/Cys",
      "p.Trp24Ter",
      "p.Trp24*",
      "p.Cys28delinsTrpVal",
      "p.Cys28_Lys29delinsTrp",
      "p.(Asn47delinsSerSerTer)",
      "p.(Glu125_Ala132delinsGlyLeuHisArgPheIleValLeu)",
      "p.Arg97ProfsTer23",
      "p.Gln151Thrfs*9",
      "p.Glu5ValfsTer5",
    )
    .pipe(
      seq => seq ++ seq.map(_.toLowerCase)   // To check that also lowercase input strings are converted correctly
    )


  "Conversion from 1 to 3 letter amino acid code" must "have been correct" in {

    forAll(
      proteinChanges
        .map(HGVS.Protein.to1LetterCode)
        .map(HGVS.Protein.to3LetterCode)
        .zip(proteinChanges)
    ){ 
      case (conv,ref) => assert(ref.replace("*","Ter") equalsIgnoreCase conv)
    }

  }


}
