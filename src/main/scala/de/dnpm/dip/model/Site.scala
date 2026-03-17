package de.dnpm.dip.model


import scala.util.{
  Try,
  Failure
}
import scala.util.Properties.{
  envOrElse,
  propOrNull
}
import de.dnpm.dip.util.Logging
import de.dnpm.dip.coding.Coding


sealed trait Site


object Site extends Logging
{

  implicit val codingSystem: Coding.System[Site] =
    Coding.System[Site]("dnpm-dip/site")


  val ENV  = "BACKEND_LOCAL_SITE"
  val PROP = "dnpm.dip.site"


  lazy val local: Coding[Site] =
    Try {
      val csv = 
        envOrElse(
          ENV,
          propOrNull(PROP)
        )
        .split("[:\\|]")
      
      val code = csv(0).trim
      val name = csv(1).trim

      Coding[Site](code,name)
    }
    .recoverWith { 
      case t => 
        log.error(
          s"Failed to load local site. This is most likely due to undefined/invalid value of either ENV variable '$ENV' or system property '$PROP': expected format '<Site Code>:<Site Name>' (valid delimiters | : )", t
        )
        Failure(t)
    }
    .get

}
