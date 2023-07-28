package de.dnpm.dip.model


final case class Issue
(
  message: String,
  severity: Issue.Severity.Value,
  location: Option[String]
)


object Issue
{
  
  object Severity extends Enumeration
  {
    val Info    = Value("info")
    val Warning = Value("warning")
    val Error   = Value("error")
    val Fatal   = Value("fatal")
  }

  def Fatal(
    msg: String,
    loc: Option[String]
  ) = Issue(msg,Severity.Fatal,loc)

  def Error(
    msg: String,
    loc: Option[String]
  ) = Issue(msg,Severity.Error,loc)

  def Warning(
    msg: String,
    loc: Option[String]
  ) = Issue(msg,Severity.Warning,loc)

  def Info(
    msg: String,
    loc: Option[String]
  ) = Issue(msg,Severity.Info,loc)

}

