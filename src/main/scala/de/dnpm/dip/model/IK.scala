package de.dnpm.dip.model


import de.dnpm.dip.coding.Coding


sealed trait IK

object IK
{
  implicit val system: Coding.System[IK] =
    Coding.System("https://www.dguv.de/arge-ik")
}

