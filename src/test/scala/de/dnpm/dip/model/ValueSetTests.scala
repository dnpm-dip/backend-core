package de.dnpm.dip.model


import java.net.URI
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import de.dnpm.dip.coding.{
  CodeSystems,
  ValueSet
}
import shapeless.{
  :+:,
  CNil
}

class ValueSetTests extends AnyFlatSpec
{

  type Systems = VitalStatus.Value :+: Gender.Value :+: CNil

  "ValueSet composition" must "have worked" in {

    val composer =
      ValueSet.compose(
        uri = URI.create("composite-valueSet"),
        name = "Composite-ValueSet",
        title = None
      )
      .includeAll[VitalStatus.Value]
      .includeAll[Gender.Value]


    val valueSet =
      composer.expand[Systems]
      
    valueSet.codings.size must equal (
      CodeSystems[Systems].values.values.flatMap(_.concepts).size
    )

  }


}
