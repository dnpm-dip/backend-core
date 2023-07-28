package de.dnpm.dip.model



final case class HumanName
(
  givenName: String,
  familyName: String,
  title: Option[String]
)

