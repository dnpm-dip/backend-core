package de.dnpm.dip.model


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

  "Deserialization of invalid Coding[Gender]" must "have failed" in {

    val coding =
      Coding(Gender.Female)
       .pipe(Json.toJson(_))
       .pipe(Json.stringify)
       .pipe(_.replace("female","fmale"))
       .pipe(Json.parse)
       .pipe(Json.fromJson[Coding[Gender.Value]](_))

    coding.isError mustBe true

  }

}
