package de.dnpm.dip.coding.ops


import de.dnpm.dip.coding.Coding


sealed trait OPS

object OPS
{

  implicit val codingSystem: Coding.System[OPS] =
    Coding.System[OPS]("OPS")

}
