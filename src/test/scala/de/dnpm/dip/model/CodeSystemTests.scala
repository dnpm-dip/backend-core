package de.dnpm.dip.model



import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import de.dnpm.dip.coding.{
  Coding,
  CodeSystemProvider
}


class CodeSystemTests extends AnyFlatSpec
{

  "Loading Enum CodeSystems dynamically via CodeSystemProvider" must "have worked" in {

    CodeSystemProvider
      .getInstances[cats.Id]
      .map(_.uri)
      .toList must contain allOf (
        Coding.System[Episode.Status.Value].uri,
        Coding.System[GuidelineTreatmentStatus.Value].uri,
        Coding.System[Therapy.Status.Value].uri,
        Coding.System[Relationship.Value].uri,
        Coding.System[Gender.Value].uri,
        Coding.System[VitalStatus.Value].uri,
        Coding.System[TherapyRecommendation.Priority.Value].uri,
      )

  }


}
