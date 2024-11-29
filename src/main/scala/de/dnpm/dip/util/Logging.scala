package de.dnpm.dip.util


import org.slf4j.LoggerFactory


trait Logging
{ 
  self =>

  val log = LoggerFactory.getLogger(self.getClass)
}
