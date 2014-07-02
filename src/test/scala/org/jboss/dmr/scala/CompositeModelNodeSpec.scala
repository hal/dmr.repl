package org.jboss.dmr.scala

import org.scalatest.{Matchers, FlatSpec}

class CompositeModelNodeSpec extends FlatSpec with Matchers {

  "A composite ModelNode made from two model nodes" should "have the composite op with two steps" in {

    val one = ModelNode("one" -> 1)
    val two = ModelNode("two" -> 2)
    val comp = ModelNode.composite(one, two)

    comp.keys should contain only("operation", "steps")
    comp("operation").asString.get shouldBe "composite"
    comp("steps").asList.get should contain inOrderOnly(one, two)
  }
}
