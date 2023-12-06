package de.dnpm.dip.model



import java.net.URI
import play.api.libs.json.{
  Json,
  JsString,
  JsError,
  JsSuccess,
  Writes,
  OWrites,
  Reads,
}
import de.dnpm.dip.coding.Coding



abstract class UnitOfMeasure
{
  val name: String
  val symbol: String

  override def toString = symbol
}

object UnitOfMeasure
{
  implicit val uri: Coding.System[UnitOfMeasure] =
    Coding.System[UnitOfMeasure]("http://unitsofmeasure.org")

  def apply(
    n: String,
    s: String
  ): UnitOfMeasure =
    new UnitOfMeasure {
      override val name   = n
      override val symbol = s
    }

  implicit def writesUnitOrMeasure[U <: UnitOfMeasure]: Writes[U] =
    Writes { u => JsString(u.toString) }

}

sealed abstract class UnitOfTime
(
  override val name: String,
  override val symbol: String
)
extends UnitOfMeasure

object UnitOfTime
{

  final case object Seconds extends UnitOfTime("Seconds","s")
  final case object Minutes extends UnitOfTime("Minutes","min")
  final case object Hours   extends UnitOfTime("Hours","h")
  final case object Days    extends UnitOfTime("Days","d")
  final case object Weeks   extends UnitOfTime("Weeks","wk")
  final case object Months  extends UnitOfTime("Months","mo")
  final case object Years   extends UnitOfTime("Years","a")

  import java.time.temporal.ChronoUnit

  val values =
    Set(
      Seconds,
      Minutes,
      Hours,
      Days,  
      Weeks,
      Months,
      Years  
    )


  val of: PartialFunction[ChronoUnit,UnitOfTime] = {

    import ChronoUnit._

    Map( 
      SECONDS -> Seconds,
      MINUTES -> Minutes,
      HOURS   -> Hours,
      DAYS    -> Days,  
      WEEKS   -> Weeks,
      MONTHS  -> Months,  
      YEARS   -> Years
    )
  }


  implicit val readsUnitOfTime: Reads[UnitOfTime] =
    Reads {
      _.validate[String]
       .flatMap {
         unit => 
           values
             .find(_.symbol == unit)
             .orElse(
               values.find(_.name.toLowerCase == unit.toLowerCase)
             ) match {
               case Some(t) => JsSuccess(t)
               case None    => JsError(s"Invalid unit of time '$unit'")
             }

       }
    }
}

// Dimensionless unit of measure
final case object One extends UnitOfMeasure
{
  override val name   = ""
  override val symbol = ""
}


abstract class Quantity
{
  val value: Double
  val unit: UnitOfMeasure

  override def toString: String =
    s"$value ${unit.symbol}"
}

object Quantity
{

  implicit def ordering[Q <: Quantity]: Ordering[Q] =
    Ordering[Double].on(_.value)

  implicit def quantityWrites[Q <: Quantity]: OWrites[Q] =
    OWrites {
      q => 
        Json.obj(
          "value" -> q.value,
          "unit" ->  q.unit.symbol
        )
    }
}


final case class SimpleQuantity
(
  value: Double,
  unit: UnitOfMeasure
)
extends Quantity



final case class Age
(
  value: Double,
  unit: UnitOfTime = UnitOfTime.Years
)
extends Quantity

object Age
{
  implicit val readsAge: Reads[Age] =
    Json.reads[Age]
}

