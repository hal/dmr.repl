package org.jboss.dmr.scala

import org.scalatest.{FlatSpec, Matchers}

// TODO
class DslModelNodeSpec extends FlatSpec with Matchers {

  "A ModelNode" should "have an empty address when created with the special 'root' address" in {
    val node = ModelNode() at root
    node.size shouldBe 1

    val address = node("address")
    address.isEmpty shouldBe true

    import org.jboss.dmr.ModelType._
    val ModelNode(addressType) = address
    addressType shouldBe LIST
  }

  it should "support a DSL expression for creation" in {
    val node = ModelNode() at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") op Operation('read_resource)(
      'include_runtime -> false,
      'recursive_depth -> 2)
    node.size shouldBe 4

    val address = node("address")
  }
}
