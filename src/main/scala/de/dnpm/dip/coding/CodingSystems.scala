package de.dnpm.dip.coding



sealed trait LOINC
object LOINC
{
  implicit val system: Coding.System[LOINC] =
    Coding.System[LOINC]("http://loinc.org")
}

sealed trait SNOMEDCT
object SNOMEDCT
{
  implicit val system: Coding.System[SNOMEDCT] =
    Coding.System[SNOMEDCT]("http://snomed.info/sct")
}
