package de.dnpm.dip.util


import java.time.YearMonth
import scala.util.chaining._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import play.api.libs.json.{
  Json,
  JsString
}


class JsonSerializationTests extends AnyFlatSpec
{

  "YearMonth" must "have been correctly deserialized" in { 

    import de.dnpm.dip.util.json.{
      readsYearMonth,
      writesYearMonth
    }

    assert(Json.fromJson[YearMonth](JsString("2025-11")).isSuccess)

    assert(Json.fromJson[YearMonth](JsString("2025-1")).isError)
    assert(Json.fromJson[YearMonth](JsString("2025-1-13")).isError)

    assert(Json.toJson(YearMonth.now).pipe(Json.fromJson[YearMonth](_)).isSuccess)

  }

}
