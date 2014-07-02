package org.jboss.dmr.scala

import org.jboss.dmr.ModelType._
import org.scalatest.{FlatSpec, Matchers}
import scala.language.reflectiveCalls

class NewModelNodeSpec extends FlatSpec with Matchers {

  def fixture = new {
    val valueNodes = List(ModelNode("foo"), ModelNode(true), ModelNode(1234), ModelNode(1234l), ModelNode(12.34d))
    val node = ModelNode("foo" -> "bar", "answer" -> 42)
  }

  "A ModelNode created from a value" should "be a ValueModelNode" in {
    for (node <- fixture.valueNodes) {
      node shouldBe a[ValueModelNode]
    }
  }

  it should "have the correct ModelType" in {
    for (node <- fixture.valueNodes) {
      node match {
        case ModelNode(STRING) | ModelNode(BOOLEAN) | ModelNode(INT) | ModelNode(LONG) | ModelNode(DOUBLE) => () // ok
        case ModelNode(unknownType) => fail(s"Unknown type: $unknownType")
      }
    }
  }

  it should "return the correct value for the as...() methods" in {
    for (node <- fixture.valueNodes) {
      node match {
        case ModelNode(STRING) => node.asString should be(Some("foo"))
        case ModelNode(BOOLEAN) => node.asBoolean should be(Some(true))
        case ModelNode(INT) => node.asInt should be(Some(1234))
        case ModelNode(LONG) => node.asLong should be(Some(1234l))
        case ModelNode(DOUBLE) => node.asDouble should be(Some(12.34d))
      }
    }
  }

  it should "return None for asList()" in {
    for (node <- fixture.valueNodes) {
      node.asList shouldBe None
    }
  }

  "A ModelNode created from key/value pairs" should "be a ComplexValueNode" in {
    fixture.node shouldBe a[ComplexModelNode]
  }

  it should "have the correct ModelType" in {
    val ModelNode(typ) = fixture.node
    typ should equal(OBJECT)
  }

  it should "return the correct value for the as...() methods" in {
    val f = fixture
    import f._
    node.asBigInt shouldBe None
    node.asBoolean shouldBe Some(true)
    node.asDouble shouldBe None
    node.asInt shouldBe Some(2)
    node.asList.get should contain inOrderOnly(ModelNode("foo" -> "bar"), ModelNode("answer" -> 42))
    node.asLong shouldBe Some(2)
    node.asString shouldBe Some( """{"foo" => "bar","answer" => 42}""")
  }
}
