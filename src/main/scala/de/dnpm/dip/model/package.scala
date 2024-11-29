package de.dnpm.dip
package object model {


import cats.{
  Applicative,
  Id
}
import shapeless.{ 
  :+:,
  CNil
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystemProvider
}
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.coding.UnregisteredMedication

  
type Medications = ATC :+: UnregisteredMedication :+: CNil

object Medications
{

  implicit class MedicationCodingProperties(val coding: Coding[Medications]) extends AnyVal
  {

    import ATC.extensions._
    
    // Get the current group an ATC Entry is classified into,
    // by resolving the entry with same name in the latest ATC version then its parent
    def currentGroup(
      implicit atc: CodeSystemProvider[ATC,Id,Applicative[Id]]
    ): Option[Coding[Medications]] =
      coding.system match {
        case sys if sys == Coding.System[ATC].uri =>
          coding.asInstanceOf[Coding[ATC]]
            .currentGroup
            .map(_.asInstanceOf[Coding[Medications]])
    
        case _ => None
      }
    
    
    def group(
      implicit atc: CodeSystemProvider[ATC,Id,Applicative[Id]]
    ): Option[Coding[Medications]] =
      coding.system match {
        case sys if sys == Coding.System[ATC].uri =>
          coding.asInstanceOf[Coding[ATC]]
            .group
            .map(_.asInstanceOf[Coding[Medications]])
    
        case _ => None
      }
  }
  
}

} 
