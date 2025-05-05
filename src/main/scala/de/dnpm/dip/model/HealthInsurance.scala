package de.dnpm.dip.model


import de.dnpm.dip.coding.{
  CodedEnum,
  DefaultCodeSystem
}


sealed trait HealthInsurance extends Organization

object HealthInsurance
{

  object Type
  extends CodedEnum("http://fhir.de/CodeSystem/versicherungsart-de-basis")
  with DefaultCodeSystem
  {
    val GKV, PKV, BG, SEL, SOZ, GPV, PPV, BEI, SKT, UNK = Value

    override val display =
      Map(
        GKV -> "gesetzliche Krankenversicherung",
        PKV -> "private Krankenversicherung",
        BG  -> "Berufsgenossenschaft",
        SEL -> "Selbstzahler",
        SOZ -> "Sozialamt",
        GPV -> "gesetzliche Pflegeversicherung",
        PPV -> "private Pflegeversicherung",
        BEI -> "Beihilfe",
        SKT -> "Sonstige Kostenträger",
        UNK -> "Unbekannt"
      )
  }

}
