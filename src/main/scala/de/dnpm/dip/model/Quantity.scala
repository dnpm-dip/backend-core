package de.dnpm.dip.model



import java.net.URI
import de.dnpm.dip.coding.Coding



abstract class UnitOfMeasure
{
  val name: String
  val symbol: String

  override def toString = symbol
}

object UnitOfMeasure
{
  implicit val uri =
    Coding.System[UnitOfMeasure]("http://unitsofmeasure.org")

  def apply(
    n: String,
    s: String
  ): UnitOfMeasure =
    new UnitOfMeasure {
      override val name   = n
      override val symbol = s
    }
    
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
  final case object Years   extends UnitOfTime("Years","a")

  import java.time.temporal.ChronoUnit

  val of: PartialFunction[ChronoUnit,UnitOfTime] = {

    import ChronoUnit._

    Map( 
      SECONDS -> Seconds,
      MINUTES -> Minutes,
      HOURS   -> Hours,
      DAYS    -> Days,  
      YEARS   -> Years
    )
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

}


final case class SimpleQuantity
(
  value: Double,
  unit: UnitOfMeasure
)
extends Quantity



final case class Age
(
  value: Double 
)
extends Quantity
{
  override val unit: UnitOfMeasure =
    UnitOfTime.Years
}




