package org.jboss.dmr.repl.samples

import org.jboss.dmr.scala._
import org.jboss.dmr.repl._
import org.jboss.dmr.repl.Response._

class RemoveServers(servers: Seq[Server]) extends Script[ModelNode] {

  override def code = {
    // skip started and non existing servers servers
    val serverNodes = servers.map(server => {
      val node = ModelNode() at ("host" -> server.host) / ("server-config" -> server.name) op 'read_attribute(
        'name -> "status")
      (server, node)
    })
    val stoppedAndExistingServerNodes = serverNodes.filter {
      case (server, node) => client ! node map {
        case Response(Success, result) => "STARTED" != (result.asString getOrElse "")
        case Response(Failure, failure) => false
      } getOrElse false
    }

    // delete remaining server using a composite
    if (stoppedAndExistingServerNodes.nonEmpty) {
      val nodes = stoppedAndExistingServerNodes.map {
        case (server, _) => ModelNode() at ("host" -> server.host) / ("server-config" -> server.name) op 'remove
      }
      client ! ModelNode.composite(nodes) map {
        case Response(Success, result) => result
        case Response(Failure, failure) => throw new ScriptException(failure)
      }
    } else
      util.Success(ModelNode("No servers removed"))
  }
}
