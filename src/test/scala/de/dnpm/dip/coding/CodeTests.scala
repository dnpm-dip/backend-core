package de.dnpm.dip.coding


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.Inspectors._
import de.dnpm.dip.coding.icd.ICD10GM


sealed trait Dummy

object Dummy
{
  implicit val system: Coding.System[Dummy] =
    Coding.System("dummy-codesystem")
}


class CodeTests extends AnyFlatSpec
{

  val coding1 =
    Coding[Dummy](
      "Code1",
      "Display for Code1",
      "1.0"
    )

  val coding1pr =
    coding1.copy(
      display = None
    )

  val coding1ppr =
    coding1.copy(
      version = Some("1.1")
    )

  val coding2 =
    Coding[Dummy](
      "Code2",
      "Display for Code2",
      "1.0"
    )


  "Coding equality check" must "be well-behaved" in {

    coding1 must equal (coding1pr)

    coding1 must equal (coding1ppr)

    coding1 must not equal coding2

  }

  "Coding hashCode check" must "be well-behaved" in {

    coding1.hashCode must equal (coding1pr.hashCode)

    coding1.hashCode must equal (coding1ppr.hashCode)

    coding1.hashCode must not equal coding2.hashCode

  }


}
