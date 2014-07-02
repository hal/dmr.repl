package org.jboss.dmr.scala

import org.scalatest.{FlatSpec, Matchers}

class ReadModelNodeSpec extends FlatSpec with Matchers {

  def fixture = new {
    val node = ModelNode("foo" -> "bar")
    val nested = ModelNode(
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

  "A ModelNode" should "return the model node associated with a path" in {
    val f = fixture
    import f._
    node("foo") should equal(ModelNode("bar"))
    node.get("foo") should equal(Some(ModelNode("bar")))
    nested("child" / "deep-inside" / "foo") should equal(ModelNode("bar"))
    nested.get("child" / "deep-inside" / "foo") should equal(Some(ModelNode("bar")))
  }

  it should "throw a NoSuchElementException for none existing paths" in {
    val f = fixture
    import f._
    intercept[NoSuchElementException] {
      node("boo")
    }
    intercept[NoSuchElementException] {
      nested("child" / "deep-inside" / "moo")
    }
  }
}
