package de.dnpm.dip.coding


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import shapeless.{
  :+:,
  CNil
}


sealed trait Foo
object Foo
{
  implicit val system: Coding.System[Foo] =
    Coding.System("foo-code-system")
}

sealed trait Bar
object Bar
{
  implicit val system: Coding.System[Bar] =
    Coding.System("bar-code-system")
}


class CodeTests extends AnyFlatSpec
{


  val coding1 =
    Coding[Foo](
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
    Coding[Foo](
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



  type FooOrBar = Foo :+: Bar :+: CNil

  "Conversion of Coding[S] to Coding[CodeSystem Union]" must "have compiled" in {
    assertCompiles(
    """
      val codings: List[Coding[FooOrBar]] =
        List(
          Coding[FooOrBar].from(Code[Foo]("foo-code")),
          Coding[FooOrBar].from(Code[Bar]("bar-code"))
        )
    """
    )
  }



}
