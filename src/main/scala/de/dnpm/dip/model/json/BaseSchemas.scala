package de.dnpm.dip.model.json


import java.time.{
  LocalDate,
  YearMonth
}
import scala.reflect.ClassTag
import scala.util.chaining._
import json.{
  Json,
  Schema
}
import json.schema.validation._
import com.github.andyglow.json.Value
import Schema.`object`.Field
import de.dnpm.dip.coding.{
  Code,
  Coding,
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.model.{
  Age,
  BaseVariant,
  ExternalId,
  GeneAlterationReference,
  Id,
  OpenEndPeriod,
  Patient,
  Period,
  Publication,
  Reference,
  ExternalReference,
  Study,
  UnitOfTime,
  VitalStatus
}
import de.dnpm.dip.model.UnitOfTime.{
  Months,
  Years
}
import shapeless.{
  Coproduct,
  Witness
}


trait BaseSchemas
{

  implicit class SchemaExtensions[T](val sch: Schema[T]){

    def toSimpleNameDefinition(implicit ct: ClassTag[T]): Schema[T] =
      sch.toDefinition(ct.runtimeClass.getSimpleName)

    def addField[V](
      name: String,
      fieldSch: Schema[V],
      required: Boolean = true
    ): Schema[T] =
      sch match {
        case obj: Schema.`object`[_] =>
          obj.withField(name,fieldSch,required)

        case x => x
      }

    def addOptField[V](
      name: String,
      fieldSch: Schema[V]
    ): Schema[T] =
      addField(name,fieldSch,false)
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
     .pipe(_.replace("$","_"))



  implicit def idSchema[T]: Schema[Id[T]] =
    Schema.`string`.asInstanceOf[Schema[Id[T]]]
      .toDefinition("Id")


  protected def externalIdSchemaOf[T,S <: Coproduct](
    definition: String
  )(
    implicit uris: Coding.System.UriSet[S],
  ): Schema[ExternalId[T,S]] =
    Schema.`object`[ExternalId[T,S]](
      Field("value",Schema.`string`),
      Field(
        "system",
        Schema.`enum`[String](
          Schema.`string`,
          uris.values.map(uri => Value.str(uri.toString))
        )
      )
    )
    .toDefinition(definition)


  implicit def externalIdSchema[T,S: Coding.System]: Schema[ExternalId[T,S]] =
    Schema.`object`[ExternalId[T,S]](
      Field("value",Schema.`string`)
    )
    .toDefinition(s"ExternalId")


/*
  protected def externalReference[T, Systems <: Coproduct](
    definition: String
  )(
    implicit systems: Coding.System.UriSet[Systems]
  ): Schema[ExternalReference[T]] =
    Schema.`object`[ExternalReference[T]](
      Field("id",Schema.`string`),
      Field(
        "system",
        Schema.`enum`[String](
          Schema.`string`,
          systems.values.map(uri => Value.str(uri.toString))
        )
      ),
      Field("type",Schema.`string`,false),
    )
    .toDefinition(definition)


  implicit val studyReferenceSchema: Schema[ExternalReference[Study]] =
    externalReference[Study,Study.Registries]("Study_Reference")

  implicit val publicationReferenceSchema: Schema[ExternalReference[Publication]] =
    externalReference[Publication,Publication.Systems]("Publication_Reference")


  implicit def externalReferenceSchema[T]: Schema[ExternalReference[T]] =
    Schema.`object`[ExternalReference[T]](
      Field("id",Schema.`string`),
      Field("system",Schema.`string`),
      Field("type",Schema.`string`,false),
    )
    .toDefinition("External_Reference")
*/

  protected def externalReferenceSchemaOf[T,S <: Coproduct](
    definition: String
  )(
    implicit
    uris: Coding.System.UriSet[S],
  ): Schema[ExternalReference[T,S]] =
    Schema.`object`[ExternalReference[T,S]](
      Field("id",Schema.`string`),
      Field(
        "system",
        Schema.`enum`[String](
          Schema.`string`,
          uris.values.map(uri => Value.str(uri.toString))
        )
      ),
      Field("display",Schema.`string`,false),
      Field("type",Schema.`string`,false),
    )
    .toDefinition(definition)


  implicit val studyReferenceSchema: Schema[ExternalReference[Study,Study.Registries]] =
    externalReferenceSchemaOf[Study,Study.Registries]("Study_Reference")

  implicit val publicationReferenceSchema: Schema[ExternalReference[Publication,Publication.Systems]] =
    externalReferenceSchemaOf[Publication,Publication.Systems]("Publication_Reference")

  implicit def defaultReferenceSchema[T]: Schema[Reference[T]] =
    Schema.`object`[Reference[T]](
      Field("id",Schema.`string`),
      Field("display",Schema.`string`,false),
      Field("system",Schema.`string`,false),
      Field("type",Schema.`string`,false)
    )
    .toDefinition("Reference")


  implicit def geneAlterationReferenceSchema[T <: BaseVariant]: Schema[GeneAlterationReference[T]] =
    Schema.`object`[GeneAlterationReference[T]](
      Field("variant",Json.schema[Reference[T]],true),
      Field("display",Schema.`string`,false),
      Field("gene",Json.schema[Coding[HGNC]],false)
    )
    .toDefinition("GeneAlterationReference")



  implicit val yearMonthSchema: Schema[YearMonth] =
    Json.schema[LocalDate]
      .asInstanceOf[Schema[YearMonth]]
      .toDefinition("YearMonth")
      .withValidation(
        Instance.pattern := "^\\d{4}\\-(0[1-9]|1[012])$"
      )(
        Magnet.mk[YearMonth,String]
      )


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


  def coproductCodingSchema[S <: Coproduct](
    implicit
    uris: Coding.System.UriSet[S],
    ns: Coding.System.Names[S],
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
    .toDefinition(s"Coding_${ns.names.mkString("_")}")


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



  implicit val datePeriodSchema: Schema[Period[LocalDate]] =
    Json.schema[OpenEndPeriod[LocalDate]]
      .asInstanceOf[Schema[Period[LocalDate]]]
      .toDefinition("Period_Date")


  implicit val ageSchema: Schema[Age] =
    Schema.`object`[Age](
      Field[Double]("value",Schema.`number`[Double]),
      Field[String](
        "unit",
        Schema.`enum`[String](
          Schema.`string`,
          Set(Months,Years).map(_.name).map(Value.str)
        )
      )
    )
    .toDefinition("Age")


  implicit val unitOfTimeSchema: Schema[UnitOfTime] =
    Schema.`enum`[UnitOfTime](
      Schema.`string`,
      UnitOfTime.values.map(_.name).map(Value.str)  
    )
    .toDefinition("UnitOfTime")

/*
  implicit val healthInsuranceRefSchema: Schema[ExternalReference[HealthInsurance,IK]] =
    defaultReferenceSchema[HealthInsurance]
      .asInstanceOf[Schema[ExternalReference[HealthInsurance,IK]]]
      .addOptField("system",Schema.`string`)
      .toDefinition("HealthInsurance_Reference")


  implicit val healthInsuranceRefSchema: Schema[Reference[HealthInsurance]] =
    defaultReferenceSchema[HealthInsurance]
      .addOptField("system",Schema.`string`)
      .toDefinition("HealthInsurance_Reference")
*/

  implicit val patientSchema: Schema[Patient] =
    Json.schema[Patient]
      .addOptField("age",Json.schema[Age])
      .addOptField("vitalStatus",Json.schema[Coding[VitalStatus.Value]])
      .toSimpleNameDefinition

}
