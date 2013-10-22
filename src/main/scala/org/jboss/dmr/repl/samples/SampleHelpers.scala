package org.jboss.dmr.repl.samples

import org.jboss.dmr.repl.{Response, Script}
import org.jboss.dmr.scala.ModelNode
import org.jboss.dmr.repl.Response._

/** Provides helper methods for the samples */
trait SampleHelpers[T] {
  this: Script[T] =>

  /** Returns the string values from a DMR operation whose result type is a list of string model nodes */
  private[samples] def stringValues(node: ModelNode): List[String] = {
    val values = client ! node map {
      case Response(Success, result) => {
        for {
          element <- result.values
          value <- element.asString
        } yield value
      }
      case Response(Failure, failure) => List() // nothing found
    } getOrElse List() // errors are ignored
    values.toList
  }
}
