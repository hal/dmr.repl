package org.jboss.dmr.scala

import org.scalatest.{Matchers, FlatSpec}

// TODO
class TraversableModelNodeSpec extends FlatSpec with Matchers {

  def fixture = new {
    val valueNode = ModelNode(42)
    val simpleNode = ModelNode("foo" -> "bar")
    val nestedNode = ModelNode(
      "flag" -> true,
      "hello" -> "world",
      "answer" -> 42,
      "child" -> ModelNode(
        "inner-a" -> 123,
        "inner-b" -> "test",
        "deep-inside" -> ModelNode("foo" -> "bar"),
        "deep-list" -> List(
          ModelNode("one" -> 1),
          ModelNode("two" -> 2),
          ModelNode("three" -> 3)
        )
      )
    )
  }

  "A ValueModelNode" should "have a depth of 0" in {
    val f = fixture
    import f._

    valueNode.depth should equal(0)
  }

  "A flat ComplexModelNode with attributes only" should "have a depth of 0" in {
    val f = fixture
    import f._

    simpleNode.depth should equal(0)
  }

  "A nested ComplexModelNode" should "have a depth greater than 0" in {
    val f = fixture
    import f._

    nestedNode.depth should equal(2)
  }
}
