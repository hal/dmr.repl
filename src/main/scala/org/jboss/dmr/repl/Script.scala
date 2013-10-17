package org.jboss.dmr.repl

import scala.util.{Failure, Try}
import org.jboss.dmr.scala._

/** Base class for scripts */
abstract class Script[T](implicit val client: Client) {

  /** Executes [[org.jboss.dmr.repl.Script.code]], if there's a client with an established connection */
  final def run(): Try[T] = client.connection match {
    case Some(con) => code
    case None => Failure(new ClientException("No connection"))
  }

  def code: Try[T]
}

case class ScriptException(msg: String) extends RuntimeException(msg) {
  def this(failure: ModelNode) = this(failure.asString getOrElse "undefined")
}
