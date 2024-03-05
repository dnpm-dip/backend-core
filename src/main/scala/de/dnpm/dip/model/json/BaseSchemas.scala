package de.dnpm.dip.model.json


import java.time.LocalDate
import java.time.temporal.Temporal
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
}
import shapeless.{
  =:!=,
  Witness
}


trait BaseSchemas
{

  implicit def idSchema[T]: Schema[Id[T]] =
    Schema.`string`.asInstanceOf[Schema[Id[T]]]
      .toDefinition("Id")


  implicit def externalIdSchema[T]: Schema[ExternalId[T]] =
    Schema.`object`[ExternalId[T]](
      Field("value",Schema.`string`),
      Field("system",Schema.`string`(Schema.`string`.Format.`uri`),false),
    )
    .toDefinition("ExternalId")


  implicit def defaultReferenceSchema[T]: Schema[Reference[T]] =
    Schema.`object`[Reference[T]](
      Field("id",Schema.`string`),
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
            )
          ),
        ),
        false
      ),
      Field("uri",Schema.`string`(Schema.`string`.Format.`uri`),false),
    )
    .toDefinition("Reference[Publication]")


  implicit def enumCodingSchema[E <: Enumeration](
    implicit w: Witness.Aux[E]
  ): Schema[Coding[E#Value]] = {
    val name =
      w.value.getClass.getName
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

    Schema.`object`[Coding[E#Value]](
      Field(
        "code",
        Schema.`enum`[Code[E#Value]](
          Schema.`string`,
          w.value.values.map(_.toString).toSet.map(Value.str)
        )
      ),
      Field("display",Schema.`string`,false),
      Field("system",Schema.`string`(Schema.`string`.Format.`uri`),false),
      Field("version",Schema.`string`,false)
    )
    .toDefinition(s"Coding[$name]")
  }



  protected def codeSchema[T]: Schema[Code[T]] =
    Schema.`string`.asInstanceOf[Schema[Code[T]]]
      .toDefinition("Code")


  implicit def codingSchema[T](
    implicit notAny: T =:!= Any
  ): Schema[Coding[T]] =
    Schema.`object`[Coding[T]](
      Field("code",codeSchema[T]),
      Field("display",Schema.`string`,false),
      Field("system",Schema.`string`(Schema.`string`.Format.`uri`),false),
      Field("version",Schema.`string`,false)
    )
    .toDefinition("Coding")

/*
  implicit val anyCodingSchema: Schema[Coding[Any]] =
    Schema.`object`[Coding[Any]](
      Field("code",codeSchema[Any]),
      Field("display",Schema.`string`,false),
      Field("system",Schema.`string`(Schema.`string`.Format.`uri`)),
      Field("version",Schema.`string`,false)
    )
    .toDefinition("Coding[Any]")
*/

  implicit val datePeriodSchema: Schema[Period[LocalDate]] =
    Json.schema[OpenEndPeriod[LocalDate]]
      .asInstanceOf[Schema[Period[LocalDate]]]


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

}

