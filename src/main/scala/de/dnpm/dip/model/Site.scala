package de.dnpm.dip.model


import scala.util.{
  Try,
  Failure
}
import de.dnpm.dip.util.Logging
import de.dnpm.dip.coding.Coding


sealed trait Site


object Site extends Logging
{

  implicit val codingSystem: Coding.System[Site] =
    Coding.System[Site]("dnpm-dip/site")


  val property = "dnpm.dip.site"


  lazy val local: Coding[Site] = {


    Try {
      val csv = 
        System.getProperty(property).split("[:\\|]")
      
      val code = csv(0).trim
      val name = csv(1).trim

      Coding[Site](code,name)
    }
    .recoverWith { 
      case t => 
        log.error(
          s"Failed to load local site. This most likely due to undefined or invalid value of system property '$property': expect format '<Site Code>:<Site Name>' (valid delimiters | : )", t
        )
        Failure(t)
    }
    .get

  }

}

