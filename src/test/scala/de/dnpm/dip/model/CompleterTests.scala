package de.dnpm.dip.model


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import de.dnpm.dip.util.Completer
import de.dnpm.dip.coding.Coding
import java.time.YearMonth


class CompleterTests extends AnyFlatSpec
{

  import Completer.syntax._

  implicit val completer: Completer[Patient] =
    pat => pat.copy(gender = pat.gender.complete)


  "Completer[Patient]" must "have worked correctly" in {

    val patient =
      Patient(
        Id[Patient]("1234567890"),
        Coding(Gender.Unknown).copy(display = None),
        YearMonth.now,
        None,
        None,
        Patient.Insurance(
          Coding(HealthInsurance.Type.GKV),
          None
        ),
        Some(Address(Address.MunicipalityCode("12345")))
      )
      

    patient.complete.gender.display mustBe defined

  }


}
