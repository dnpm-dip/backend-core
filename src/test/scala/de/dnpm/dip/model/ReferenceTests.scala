package de.dnpm.dip.model


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import shapeless.{
  :+:, CNil
}


case class Foo
(
  id: Id[Foo],
  value: Double
)

case class Bar(
  id: Id[Bar],
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

  type FooBar = Foo :+: Bar :+: CNil

  "TypeName resolution for union types (Coproduct)" must "have worked" in { 

    Reference.TypeName[FooBar].value mustBe "Foo|Bar"
    

  }

}
