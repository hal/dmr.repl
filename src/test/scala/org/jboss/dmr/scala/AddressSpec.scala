package org.jboss.dmr.scala

import org.scalatest.{Matchers, FlatSpec}

class AddressSpec extends FlatSpec with Matchers {

  "An Address" should "be combinable using the '/' operator" in {
    val ab = Address(List(("a", "b")))
    val cd = Address(List(("c", "d")))
    val address = ab / cd
    address.tuple should contain inOrderOnly(("a", "b"), ("c", "d"))
  }

  it should "use an implicit conversion from (String, String)" in {
    val address = ("a" -> "b") / ("c" -> "d")
    address.tuple should contain inOrderOnly(("a", "b"), ("c", "d"))
  }
}
