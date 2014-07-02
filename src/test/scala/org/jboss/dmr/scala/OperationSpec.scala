package org.jboss.dmr.scala

import org.scalatest.{Matchers, FlatSpec}

class OperationSpec extends FlatSpec with Matchers {

  "An Operation" should "replace '_' with '-' for its name" in {
    val op = Operation('symbol_with_underscores)
    op.name should equal(Symbol("symbol-with-underscores"))
  }

  it should "replace '_' with '-' for its parameters" in {
    val op = Operation('foo) {
      'param_1 -> "foo"
    }
    val (key, _) = op.params.head
    key should equal(Symbol("param-1"))
  }
}
