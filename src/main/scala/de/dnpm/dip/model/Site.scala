package de.dnpm.dip.model


import play.api.libs.json.Json
import de.dnpm.dip.coding.Coding


sealed trait Site
object Site
{
  implicit val codingSystem: Coding.System[Site] =
    Coding.System[Site]("dnpm-dip/site")
}

