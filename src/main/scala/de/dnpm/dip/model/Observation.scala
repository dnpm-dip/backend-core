package de.dnpm.dip.model


import de.dnpm.dip.coding.{
  Coding
}

abstract class Observation[T: Observation.ValidValue]
{
  val id: Id[Observation[_]]
  val patient: Reference[Patient]
  val value: T
}


object Observation
{
  
  @annotation.implicitNotFound("${T} is not a valid Observation.value type")
  sealed abstract class ValidValue[T]

  private object ValidValue
  {

    def apply[T](implicit v: ValidValue[T]) = v

    implicit val intValue: ValidValue[Int] =
      new ValidValue[Int]{}

    implicit val longValue: ValidValue[Long] =
      new ValidValue[Long]{}

    implicit val floatValue: ValidValue[Float] =
      new ValidValue[Float]{}

    implicit val doubleValue: ValidValue[Double] =
      new ValidValue[Double]{}

    implicit val boolValue: ValidValue[Boolean] =
      new ValidValue[Boolean]{}

    implicit val stringValue: ValidValue[String] =
      new ValidValue[String]{}

    implicit def quantityValue[T <: Quantity]: ValidValue[T] =
      new ValidValue[T]{}

    implicit def codingValue[T <: Coding[_]]: ValidValue[T] =
      new ValidValue[T]{}

    implicit def intervalValue[T <: Interval[_]]: ValidValue[T] =
      new ValidValue[T]{}


    // Helper type class for value types 
    sealed trait ValueTypeOf[T]{ type Result }
    object ValueTypeOf
    {
      import shapeless.{HList, ::, HNil, Generic}
 
      type Aux[T,V] = ValueTypeOf[T]{ type Result = V }

      def apply[T](implicit vt: ValueTypeOf[T]): Aux[T,vt.Result] = vt

      implicit def generic[T,Tpr,V](
        implicit
        gen: Generic.Aux[T,Tpr],
        vt: Aux[Tpr,V] 
      ): Aux[T,V] =
        new ValueTypeOf[T]{ type Result = V }

      implicit def monoHList[T]: Aux[T :: HNil,T] =
        new ValueTypeOf[T :: HNil]{ type Result = T } 

    }

      
    implicit def validValueType[
      T, V
    ](
      implicit
      vt: ValueTypeOf.Aux[T,V],
      valid: ValidValue[V]
    ): ValidValue[T] =
      new ValidValue[T]{}


    //TODO: Other types
  }
  
} 
