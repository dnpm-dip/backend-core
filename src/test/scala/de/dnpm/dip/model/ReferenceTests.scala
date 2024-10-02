package de.dnpm.dip.model


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._


case class Foo
(
  id: Id[Foo],
  value: Double
)


class ReferenceTests extends AnyFlatSpec
{

  val foo =
    Foo(Id("123456789"),3.1415)

  implicit val foos: Seq[Foo] =
    Seq(foo)



  "Reference resolution" must "have worked" in {

    Reference.to(foo).resolve must be (defined)

  }

}
