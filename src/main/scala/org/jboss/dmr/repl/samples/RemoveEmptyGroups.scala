package org.jboss.dmr.repl.samples

import org.jboss.dmr.repl.Response._
import org.jboss.dmr.repl._
import org.jboss.dmr.scala._
import scala.util.Try

/** Removes empty server groups without assigned servers */
class RemoveEmptyGroups extends Script[ModelNode] with SampleHelpers[ModelNode] {

  override def code: Try[ModelNode] = {
    // read host names
    val readHosts = ModelNode() at root op 'read_children_names('child_type -> "host")
    val hosts = stringValues(readHosts)

    // read groups attribute from all servers in all hosts
    val nodes = hosts.map(host => ModelNode() at ("host" -> host) / ("server-config" -> "*") op 'read_attribute(
      'name -> "group"))
    val groupsWithServers = client ! ModelNode.composite(nodes) map {
      case Composite(Success, steps) => {
        for {
          step <- steps // each step contains a list of server configs
          server <- step.values
          group <- server.get("result")
          name <- group.asString
        } yield name
      }
      case Composite(Failure, failure) => List()
    } getOrElse List()

    // read group names and diff with active groups
    val readGroups = ModelNode() at root op 'read_children_names('child_type -> "server-group")
    val groupsToRemove = stringValues(readGroups) diff groupsWithServers.distinct

    // delete empty server groups
    if (groupsToRemove.nonEmpty) {
      val nodes = groupsToRemove.map(group => ModelNode() at ("server-group" -> group) op 'remove)
      client ! ModelNode.composite(nodes) map {
        case Response(Success, result) => result
        case Response(Failure, failure) => throw new ScriptException(failure)
      }
    } else
      util.Success(ModelNode("No server groups removed"))
  }
}
