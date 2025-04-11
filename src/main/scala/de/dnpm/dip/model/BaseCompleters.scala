package de.dnpm.dip.model


import cats.{
  Applicative,
  Id
}
import shapeless.{
  Coproduct,
  :+:,
  CNil
}
import de.dnpm.dip.util.Completer
import de.dnpm.dip.coding.{
  Code,
  Coding,
  CodeSystem,
  CodeSystemProvider,
  CodeSystemProviders,
  UnregisteredMedication
}
import de.dnpm.dip.coding.hgvs.HGVS
import de.dnpm.dip.coding.hgnc.HGNC



trait BaseCompleters
{

  import Completer.syntax._


  implicit def hgvsCompleter[S <: HGVS]: Completer[Coding[S]] =
    Coding.completeDisplayWithCode


  implicit val unregisteredCodingCompleter: Completer[Coding[UnregisteredMedication]] =
    coding => coding.copy(
      display = coding.display.orElse(Some(coding.code.value))
    )


  implicit def coproductCodingCompleter[
    H: Coding.System,
    T <: Coproduct
  ](
    implicit
    compH: Completer[Coding[H]],
    compT: Completer[Coding[T]]
  ): Completer[Coding[H :+: T]] =
    coding =>
      (
        if (coding.system == Coding.System[H].uri)
          compH(coding.asInstanceOf[Coding[H]])
        else
          compT(coding.asInstanceOf[Coding[T]])
      )
      .asInstanceOf[Coding[H :+: T]]

  implicit def terminalCoproductCodingCompleter[H: Coding.System](
    implicit compH: Completer[Coding[H]],
  ): Completer[Coding[H :+: CNil]] =
    compH.asInstanceOf[Completer[Coding[H :+: CNil]]]



  @deprecated("Might be removed in favour of descendant Tree handling","")
  private def expandDescendants[T,U >: T](
    coding: Coding[T],
    cs: CodeSystem[U]
  ): Set[Coding[T]] =
    (cs.concept(coding.code).toSet ++ cs.descendantsOf(coding.code))
      .map(
        _.toCoding(coding.system)
         .asInstanceOf[Coding[T]]
      )

  @deprecated("Might be removed in favour of descendant Tree handling","")
  def expandDescendants[T,U >: T](
    coding: Coding[T],
    csp: CodeSystemProvider[U,Id,Applicative[Id]]
  ): Set[Coding[T]] =
    expandDescendants(
      coding,
      coding.version
        .flatMap(csp.get)
        .getOrElse(csp.latest)
    )


  @deprecated("Might be removed in favour of descendant Tree handling","")
  def expandDescendantCodings[T: Coding.System](
    code: Code[T]
  )(
    implicit csp: => CodeSystemProvider[T,Id,Applicative[Id]]
  ): Set[Coding[T]] = {

    val cs = csp.latest

    (cs.concept(code).toSet ++ cs.descendantsOf(code))
      .map(_.toCoding)
  }

  @deprecated("Might be removed in favour of descendant Tree handling","")
  def expandDescendants[T](
    coding: Coding[T]
  )(
    implicit csp: => CodeSystemProvider[T,Id,Applicative[Id]]
  ): Set[Coding[T]] =
    expandDescendants(
      coding,
      coding.version
        .flatMap(csp.get)
        .getOrElse(csp.latest)
    )


  // By-name csp parameter (i.e. "lazy" as only evaluated upon being referenced)
  // is required because in traits, the value is usually not yet initialized at this point,
  // resulting in weird null pointer exception
  @deprecated("Might be removed in favour of descendant Tree handling","")
  def descendantExpander[T: Coding.System](
    implicit csp: => CodeSystemProvider[T,Id,Applicative[Id]]
  ): Completer[Set[Coding[T]]] =
    Completer.of(
      _.flatMap(coding => expandDescendants(coding,csp))
    )


  // By-name csps parameter (i.e. "lazy" as only evaluated upon being referenced)
  // is required because in traits, the value is usually not yet initialized at this point,
  // resulting in weird null pointer exception
  @deprecated("Might be removed in favour of descendant Tree handling","")
  def descendantExpanderOf[CS <: Coproduct](
    implicit csps: => CodeSystemProviders[CS]
  ): Completer[Set[Coding[CS]]] =
    Completer.of(
      _.flatMap(
        coding =>
          expandDescendants(
            coding,
            csps.values(coding.system))  // Map.apply safe here, because the code won't compile
          )                              // if not all CodeSystemProviders are in scope, so csps is sure to contain all systems
    )


  implicit def geneAlterationReferenceCompleter[T <: BaseVariant](
    implicit hgnc: CodeSystemProvider[HGNC,Id,Applicative[Id]]
  ): Completer[GeneAlterationReference[T]] =
    ref => ref.copy(
      gene = ref.gene.complete
    )


  implicit val patientCompleter: Completer[Patient] =
    pat => pat.copy(
      gender       = pat.gender.complete,
      managingSite = Some(Site.local),
      healthInsurance = pat.healthInsurance.copy(
        `type` = pat.healthInsurance.`type`.complete
      )
    )

  implicit val followUpCompleter: Completer[FollowUp] =
    fu => fu.copy(
      patientStatus = fu.patientStatus.complete
/*      
        fu.patientStatus.complete
          .orElse(
            fu.lastContactDate match { 
              case Some(_) => None
              case None => Some(Coding(FollowUp.PatientStatus.LostToFU))
            }
          )
*/        
    )

}
