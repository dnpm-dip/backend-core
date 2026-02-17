package de.dnpm.dip.model

import java.time.YearMonth
import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import play.api.libs.json.{
  Json,
  JsString
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

  "Tolerant YearMonth deserialization" must "have worked" in { 

    import Patient.tolerantReadsYearMonth

    assert(Json.fromJson[YearMonth](JsString("2025-11")).isSuccess)
    assert(Json.fromJson[YearMonth](JsString("2025-11-13")).isSuccess)

    assert(Json.fromJson[YearMonth](JsString("2025-1")).isError)
    assert(Json.fromJson[YearMonth](JsString("2025-1-13")).isError)

  }


  "Tolerant LocalDate deserialization" must "have worked" in { 

    import Patient.tolerantReadsLocalDate

    assert(tolerantReadsLocalDate.reads(JsString("2025-11")).isSuccess)
    assert(tolerantReadsLocalDate.reads(JsString("2025-11-13")).isSuccess)

    assert(tolerantReadsLocalDate.reads(JsString("2025-1")).isError)
    assert(tolerantReadsLocalDate.reads(JsString("2025-1-13")).isError)

  }

}
