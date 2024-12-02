package de.dnpm.dip.model


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import de.dnpm.dip.util.Completer
import de.dnpm.dip.coding.Coding
import java.time.LocalDate


class CompleterTests extends AnyFlatSpec
{

  import Completer.syntax._

  implicit val completer: Completer[Patient] =
    Completer.of(
      pat => pat.copy(gender = pat.gender.complete)
    )


  "Completer[Patient]" must "have worked correctly" in {

    val patient =
      Patient(
        Id[Patient]("1234567890"),
        Coding(Gender.Unknown).copy(display = None),
        LocalDate.now,
        None,
        None,
        None,
        None
      )
      

    patient.complete.gender.display mustBe defined

  }


}
