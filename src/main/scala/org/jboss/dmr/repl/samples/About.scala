package org.jboss.dmr.repl.samples

import org.jboss.dmr.scala._
import org.jboss.dmr.repl._
import org.jboss.dmr.repl.Response.{Success, Failure}

/** Simplest script one can imagine, which reads and returns the root node */
class About extends Script[ModelNode] {
  def code = {
    val node = ModelNode() at root op 'read_resource
    client ! node map {
      case Response(Success, result) => result
      case Response(Failure, failure) => throw new ScriptException(failure)
    }
  }
}
