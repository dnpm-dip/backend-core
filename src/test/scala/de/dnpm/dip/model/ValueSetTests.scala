package de.dnpm.dip.model


import java.net.URI
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodeSystems,
  ValueSet
}


class ValueSetTests extends AnyFlatSpec
{

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
      composer.expand[(VitalStatus.Value,Gender.Value)]
      
    valueSet.codings.size must equal (
      CodeSystems[(VitalStatus.Value,Gender.Value)].values.flatMap(_.concepts).size
    )

  }


}
