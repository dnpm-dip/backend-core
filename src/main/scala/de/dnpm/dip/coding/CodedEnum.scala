package de.dnpm.dip.coding


import cats.Applicative



abstract class CodedEnum
(
  private val uri: String 
)
extends Enumeration
{
  self =>

  implicit val system: Coding.System[Value] =
    Coding.System[Value](uri)

  implicit val codeSystem: CodeSystem[Value]


  def unapply(code: String): Option[Value] =
    self.values.find(_.toString == code)

  def unapply(coding: Coding[Value]): Option[Value] =
    self.unapply(coding.code.value)


  object Provider extends SingleCodeSystemProvider[Value]

  /*
   NOTE: each sub-class should have an inner class
   to provide an instance of above Provider.Facade in order to be dynamically loadable
   via CodeSystemProvider.getInstances[F...]

   final class ProviderSPI extends CodeSystemProviderSPI
   {
     override def getInstance[F[_]]: CodeSystemProvider[Any,F,Applicative[F]] =
       new Provider.Facade[F]
   }
  */
}


trait DefaultCodeSystem
{
  self: CodedEnum =>

  protected val name =
    Coding.System[Value].uri.toString


  protected val display: Value => String =
    v => {
      val s = v.toString
      s.substring(0,1).toUpperCase + s.substring(1)
    }


  // Lazy initialization required for ValueSet to be non-empty!
  // (See "Programming in Scala, 4th Ed., p. 421)
  implicit lazy val codeSystem: CodeSystem[Value] = 
    CodeSystem.of(
      uri = Coding.System[Value].uri,
      name = name,
      title = Some(name),
      version = None,
      concepts = self.values
        .toSeq
        .map(v => v -> display(v)): _*
    )

}
