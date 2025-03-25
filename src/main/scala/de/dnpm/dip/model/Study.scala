package de.dnpm.dip.model


import de.dnpm.dip.coding.Coding
import shapeless.{ 
  :+:,
  CNil
}


sealed trait Study

object Study
{
  object Registries
  {

    sealed trait NCT
    object NCT
    {
      implicit val system: Coding.System[NCT] = Coding.System("NCT")
    }
    
    sealed trait DRKS
    object DRKS
    {
      implicit val system: Coding.System[DRKS] = Coding.System("DRKS")
    }
    
    sealed trait EudraCT
    object EudraCT
    {
      implicit val system: Coding.System[EudraCT] = Coding.System("Eudra-CT")
    }
    
    sealed trait EUDAMED
    object EUDAMED
    {
      implicit val system: Coding.System[EUDAMED] = Coding.System("EUDAMED")
    }

  }

  import Registries._

  type Registries = NCT :+: DRKS :+: EudraCT :+: EUDAMED :+: CNil

}
