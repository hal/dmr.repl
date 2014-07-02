package org.jboss.dmr.scala

import org.scalatest.{Matchers, FlatSpec}

class PathSpec extends FlatSpec with Matchers {

  "A Path" should "be combinable using the '/' operator" in {
    val ab = Path(List("a", "b"))
    val cd = Path(List("c", "d"))
    val abcd = ab / cd
    abcd.elements should contain inOrderOnly("a", "b", "c", "d")
  }

  it should "use an implicit conversion from String" in {
    val abcd = "a" / "b" / "c" / "d"
    abcd.elements should contain inOrderOnly("a", "b", "c", "d")
  }
}
