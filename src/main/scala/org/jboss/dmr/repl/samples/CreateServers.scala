package org.jboss.dmr.repl.samples

import org.jboss.dmr.repl.Response._
import org.jboss.dmr.repl._
import org.jboss.dmr.scala._

/**
 * Creates the servers provided as constructor parameter. Hosts specified in the server instances must exist, whereas
 * groups are created on demand.
 */
class CreateServers(servers: Seq[Server], portOffset: Int = 10) extends Script[ModelNode] with SampleHelpers[ModelNode] {

  override def code = {
    // create non existing groups using a composite operation
    val groups = stringValues(ModelNode() at root op 'read_children_names('child_type -> "server-group"))
    val serversWithNonExistingGroup = servers.filter(server => !groups.contains(server.group))
    val groupsToCreate = serversWithNonExistingGroup.map(_.group).distinct
    val goon = if (groupsToCreate.isEmpty) util.Success(ModelNode())
    else {
      val nodes = groupsToCreate.map(group => ModelNode() at ("server-group" -> group) op 'add(
        'profile -> "full",
        'socket_binding_group -> "full-sockets")
      )
      client ! ModelNode.composite(nodes)
    }

    // create servers in another composite
    goon match {
      case util.Success(_) => {
        val hosts = stringValues(ModelNode() at root op 'read_children_names('child_type -> "host"))
        val serversWithExistingHosts = servers.filter(server => hosts.contains(server.host)).distinct
        val nodes = serversWithExistingHosts.zipWithIndex map {
          case (server, index) => {
            ModelNode() at ("host" -> server.host) / ("server-config" -> server.name) op 'add(
              'group -> server.group,
              'socket_binding_group -> "full-sockets",
              'socket_binding_port_offset -> (index * portOffset + portOffset),
              'auto_start -> false
            )
          }
        }
        client ! ModelNode.composite(nodes) map {
          case Response(Success, result) => result
          case Response(Failure, failure) => throw new ScriptException(failure)
        }
      }
      case util.Failure(ex) => throw new ScriptException(s"Failed to create servers groups: $ex")
    }
  }
}
