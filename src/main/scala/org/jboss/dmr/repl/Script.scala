package org.jboss.dmr.repl

import scala.util.{Failure, Try}
import org.jboss.dmr.scala._

abstract class Script[T](implicit val client: Client) {

  final def run(): Try[T] = client.connection match {
    case Some(con) => code
    case None => Failure(new ClientException("No connection"))
  }

  def code: Try[T]
}

case class ScriptException(msg: String) extends RuntimeException(msg) {
  def this(failure: ModelNode) = this(failure.asString getOrElse "undefined")
}
