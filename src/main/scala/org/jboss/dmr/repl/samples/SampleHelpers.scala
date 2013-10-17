package org.jboss.dmr.repl.samples

import org.jboss.dmr.repl.{Response, Script}
import org.jboss.dmr.scala.ModelNode
import org.jboss.dmr.repl.Response._

trait SampleHelpers[T] {
  this: Script[T] =>

  private[samples] def stringValuesFromResultList(node: ModelNode): List[String] = {
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
