package de.dnpm.dip.model


import java.net.URL

final case class Attachment
(
  title: Option[String],
  data: Option[Array[Byte]],
  url: Option[URL],
  hash: Option[Array[Byte]],
  contentType: Option[String]
)
