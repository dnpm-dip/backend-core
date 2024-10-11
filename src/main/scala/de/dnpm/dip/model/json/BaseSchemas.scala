package de.dnpm.dip.model.json


import java.time.{
  LocalDate,
  YearMonth
}
import java.time.temporal.Temporal
import scala.reflect.ClassTag
import scala.util.chaining._
import cats.data.NonEmptyList
import play.api.libs.json.JsObject
import json.{
  Json,
  Schema
}
import com.github.andyglow.json.Value
import com.github.andyglow.jsonschema.AsPlay._
import com.github.andyglow.jsonschema.CatsSupport._
import Schema.`object`.Field
import json.schema.Version._
import de.dnpm.dip.coding.{
  Code,
  CodedEnum,
  Coding,
  CodeSystem
}
import de.dnpm.dip.model.{
  Age,
  ExternalId,
  Id,
  Patient,
  Period,
  Publication,
  PubMed,
  OpenEndPeriod,
  Reference,
  UnitOfTime
}
import shapeless.{
  Coproduct,
  =:!=,
  Witness
}


trait BaseSchemas
{

  implicit class SchemaExtensions[T](val sch: Schema[T]){

    def toSimpleNameDefinition(implicit ct: ClassTag[T]): Schema[T] =
      sch.toDefinition(ct.runtimeClass.getSimpleName)
  }


  private def enumDefName[E <: Enumeration](
    e: E
  ): String =
    e.getClass.getName
     .pipe {
        name =>
          val idx = name lastIndexOf "."
          if (idx > 0) name.substring(idx+1,name.length)
          else name
     }
     .pipe {
       name =>
         if (name endsWith "$") name.substring(0,name.length - 1)
         else name
     }
     .pipe(_.replace("$","."))



  implicit def idSchema[T]: Schema[Id[T]] =
    Schema.`string`.asInstanceOf[Schema[Id[T]]]
      .toDefinition("Id")


  implicit def externalIdSchema[T]: Schema[ExternalId[T]] =
    Schema.`object`[ExternalId[T]](
      Field("value",Schema.`string`),
      Field("system",Schema.`string`,false),
    )
    .toDefinition("ExternalId")


  implicit def defaultReferenceSchema[T]: Schema[Reference[T]] =
    Schema.`object`[Reference[T]](
      Field("id",Schema.`string`),
      Field("display",Schema.`string`,false),
      Field("type",Schema.`string`,false),
    )
    .toDefinition("Reference")


  implicit val publicationReferenceSchema: Schema[Reference[Publication]] =
    Schema.`object`[Reference[Publication]](
      Field(
        "extId",
        Schema.`object`[ExternalId[Publication]](
          Field("value",Schema.`string`),
          Field(
            "system",
            Schema.`enum`[String](
              Schema.`string`,
              Set(Coding.System[PubMed].uri.toString).map(Value.str)
            ),
            false,
            Coding.System[PubMed].uri.toString
          ),
        ),
        false
      ),
      Field("uri",Schema.`string`,false),
      Field("type",Schema.`string`,false),
    )
    .toDefinition("Reference_Publication")


/*
  implicit val yearMonthSchema: Schema[YearMonth] =
    Json.schema[LocalDate]
      .asInstanceOf[Schema[YearMonth]]
      .toDefinition("YearMonth")
*/

  implicit val yearMonthSchema: Schema[YearMonth] = {
    
    import json.schema.validation._
    
    Json.schema[LocalDate]
      .asInstanceOf[Schema[YearMonth]]
      .toDefinition("YearMonth")
      .withValidation(
        Instance.pattern := "^\\d{4}\\-(0[1-9]|1[012])$"
      )(
        Magnet.mk[YearMonth,String]
      )
  }


  implicit def enumCodingSchema[E <: Enumeration](
    implicit w: Witness.Aux[E]
  ): Schema[Coding[E#Value]] = 
    Schema.`object`[Coding[E#Value]](
      Field(
        "code",
        Schema.`enum`[E#Value](
          Schema.`string`,
          w.value.values.map(_.toString).toSet.map(Value.str)
        )
      ),
      Field("display",Schema.`string`,false),
      Field("system",Schema.`string`,false),
      Field("version",Schema.`string`,false)
    )
    .toDefinition(s"Coding_${enumDefName(w.value)}")


  implicit def enumValueSchema[E <: Enumeration](
    implicit w: Witness.Aux[E]
  ): Schema[E#Value] =
    Schema.`enum`[E#Value](
      Schema.`string`,
      w.value.values.map(_.toString).toSet.map(Value.str)
    )
    .toDefinition(enumDefName(w.value))


  protected def codeSchema[T]: Schema[Code[T]] =
    Schema.`string`
      .asInstanceOf[Schema[Code[T]]]
      .toDefinition("Code")


  implicit def codingSchema[T]: Schema[Coding[T]] =
    Schema.`object`[Coding[T]](
      Field("code",codeSchema[T]),
      Field("display",Schema.`string`,false),
      Field("system",Schema.`string`,false),
      Field("version",Schema.`string`,false)
    )
    .toDefinition("Coding")


  def coproductCodingSchema[S <: Coproduct](
    implicit uris: Coding.System.UriSet[S]
  ): Schema[Coding[S]] =
    Schema.`object`[Coding[S]](
      Field("code",codeSchema[Any]),
      Field("display",Schema.`string`,false),
      Field(
        "system",
        Schema.`enum`[String](
          Schema.`string`,
          uris.values.map(_.toString).map(Value.str)
        ),
        true
      ),
      Field("version",Schema.`string`,false)
    )


  implicit val datePeriodSchema: Schema[Period[LocalDate]] =
    Json.schema[OpenEndPeriod[LocalDate]]
      .asInstanceOf[Schema[Period[LocalDate]]]
      .toDefinition("Period_LocalDate")


  import de.dnpm.dip.model.UnitOfTime.{Months,Years}

  implicit val ageSchema: Schema[Age] =
    Schema.`object`[Age](
      Field[Double]("value",Schema.`number`[Double]),
      Field[String](
        "unit",
        Schema.`enum`[String](
          Schema.`string`,
          Set(Months,Years).map(_.name).map(Value.str)
        )
      ),
    )
    .toDefinition("Age")


  implicit val unitOfTimeSchema: Schema[UnitOfTime] =
    Schema.`enum`[UnitOfTime](
      Schema.`string`,
      UnitOfTime.values.map(_.name).map(Value.str)  
    )
    .toDefinition("UnitOfTime")
  

}

