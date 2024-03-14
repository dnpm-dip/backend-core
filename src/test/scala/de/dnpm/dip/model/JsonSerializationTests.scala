package de.dnpm.dip.model


import java.time.LocalDate
import java.util.UUID.randomUUID
import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import play.api.libs.json.{
  Json,
  JsError,
  JsSuccess
}
import de.dnpm.dip.coding.Coding


class JsonSerializationTests extends AnyFlatSpec
{

  "Deserialization of invalid Patient-gender" must "have failed" in {

    val patient =
      Patient(
        Id[Patient]("1234567890"),
        Coding(Gender.Female),
        LocalDate.now,
        None,
        None,
        None,
      )
      .pipe(Json.toJson(_))
      .pipe(Json.prettyPrint)
      .pipe(_.replace("female","fmale"))
      .pipe(Json.parse)
      .pipe(Json.fromJson[Patient](_))

    patient.isError mustBe true

  }

}
