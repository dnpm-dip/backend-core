package de.dnpm.dip.model

import java.time.{
  LocalDate,
  YearMonth
}
import java.time.Month.JANUARY
import java.time.format.DateTimeFormatter
import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.OptionValues._
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


  "Tolerant Patient deserialization" must "have worked" in { 

    val dateOfDeath = LocalDate.of(2000,JANUARY,15) 

    val jsResult =
      Json.fromJson[Patient](
        Json.parse(
          s"""
          { 
            "id": "123456",
            "gender": {"code": "unknown"},
            "birthDate": "1970-01",
            "dateOfDeath": "${YearMonth.from(dateOfDeath).format(DateTimeFormatter.ofPattern("yyyy-MM"))}",
            "healthInsurance": {
              "type": {"code": "UNK"}
            }
          }
          """
        )
      )

    assert(jsResult.isSuccess)

    // Ensure the dateOfDeath parsed from yyyy-MM was set to yyyy-MM-01
    assert(jsResult.get.dateOfDeath.value == dateOfDeath.withDayOfMonth(1))

  }

}
