package org.jboss.dmr.scala

import org.jboss.dmr.ModelNode

/**
 * Collection of utilities to enrich the scala shell
 */
object ShellCommands {

  import java.io.ByteArrayInputStream

  val storageDelegate = new Storage()

  def connect(host: String = "127.0.0.1", port: Int = 9999): ControllerClient = {
    val client = createClient
    client.connect(host, port)
    client
  }

  def createClient = new ControllerClient

  def storage() = storageDelegate

  def fromBase64(base64: String): Option[JavaModelNode] = {
    try {
      Some(
        JavaModelNode.fromBase64(
          new ByteArrayInputStream(base64.getBytes)
        )
      )
    } catch {
      case ex: java.lang.Throwable => None
    }
  }
}
