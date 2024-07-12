package de.dnpm.dip
package object model {


import shapeless.{ 
  Coproduct,
  :+:,
  CNil
}
import de.dnpm.dip.coding.atc.ATC
import de.dnpm.dip.coding.UnregisteredMedication

  
type Medications = ATC :+: UnregisteredMedication :+: CNil
  
} 
