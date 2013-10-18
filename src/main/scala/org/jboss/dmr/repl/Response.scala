package org.jboss.dmr.repl

import scala.Some
import org.jboss.dmr.scala.ModelNode
import org.jboss.dmr.repl.Response._

/** Response constants and extractor for parsing responses to DMR operations */
object Response {
  val Success = "success"
  val Failure = "failed"

  /**
   * Extractor for matching the response of a DMR operation.
   *
   * {{{
   * val node = ... // a model node returned by some DMR operation
   * node match {
   *   case Response(Success, result) => println(s"Successful DMR operation: $result")
   *   case Response(Failure, failure) => println(s"DMR operation failed: $failure")
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
      case _ => None
    }
  }
}

/** Extractor for parsing responses to composite operations */
object Composite {

  /**
   * Extractor for matching the response of composite DMR operations.
   *
   * {{{
   * val node = ... // a model node returned by some composite DMR operation
   * node match {
   *   case Composite(Success, steps) => steps.foreach(step => s"Processing step $step")
   *   case Composite(Failure, failure) => println(s"DMR operation failed: $failure")
   *   case _ => println("Undefined result")
   * }
   * }}}
   *
   * @param node the node to match
   * @return the matched patterns
   */
  def unapply(node: ModelNode): Option[(String, List[ModelNode])] = {
    node match {
      case Response(Success, result) => {
        val results = for {
          (step, node) <- result
          stepResult <- node.get("result")
        } yield stepResult
        Some(Success -> results.toList)
      }
      case Response(Failure, failure) => Some(Failure -> List(failure))
      case _ => None
    }
  }
}
