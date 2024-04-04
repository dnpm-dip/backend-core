package de.dnpm.dip.model


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._



class SiteLoadingTests extends AnyFlatSpec
{

  System.setProperty(Site.property,"UKx:Musterlingen")


  "Local site" must "have been correctly loaded" in {

    println(Site.local)

  }


}
