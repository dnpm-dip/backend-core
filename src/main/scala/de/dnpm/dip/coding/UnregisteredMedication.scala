package de.dnpm.dip.coding


sealed trait UnregisteredMedication

object UnregisteredMedication
{
  implicit val system: Coding.System[UnregisteredMedication] =
    Coding.System[UnregisteredMedication]("undefined")
//    Coding.System[UnregisteredMedication]("-")
}
