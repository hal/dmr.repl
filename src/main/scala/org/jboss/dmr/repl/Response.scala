package org.jboss.dmr.repl

import org.jboss.dmr.scala.ModelNode

/** Response constants and extractor for parsing the response of a DMR operation  */
object Response {
  val Success = "success"
  val Failure = "failed"

  /**
   * Extractor for matching the response of a DMR operation.
   *
   * {{{
   * val node = ... // a model node returned by some DMR operation
   * node match {
   *   case ModelNode(Response.Success, result) => println(s"Successful DMR operation: $result")
   *   case ModelNode(Response.Failure, failure) => println(s"DMR operation failed: $failure")
   *   case _ => println("Undefined result")
   * }
   * }}}
   *
   * The pattern matching variables `result` and `failure` are both model nodes containing the response payload or the
   * wrapped error description.
   *
   * @param node the node to match
   * @return the matched patterns
   */
  def unapply(node: ModelNode): Option[(String, ModelNode)] = {
    val outcome = for {
      outcomeNode <- node.get("outcome")
      outcome <- outcomeNode.asString
    } yield outcome
    outcome match {
      case Some(Success) => Some(Success -> node.getOrElse("result", ModelNode.Undefined))
      case Some(Failure) => Some(Failure -> node.getOrElse("failure-description", ModelNode("No failure-description provided")))
      case Some(undefined) => None
      case None => None
    }
  }
}
