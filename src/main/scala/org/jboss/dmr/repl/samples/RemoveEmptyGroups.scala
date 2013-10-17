package org.jboss.dmr.repl.samples

import org.jboss.dmr.scala._
import org.jboss.dmr.repl._
import org.jboss.dmr.repl.Response._
import scala.util.Try

class RemoveEmptyGroups extends Script[ModelNode] with SampleHelpers[ModelNode] {

  override def code: Try[ModelNode] = {
    val readHosts = ModelNode() at root op 'read_children_names('child_type -> "host")
    val hosts = stringValuesFromResultList(readHosts)

    // read groups  attribute from all servers in all hosts
    val nodes = hosts.map(host => ModelNode() at ("host" -> host) / ("server-config" -> "*") op 'read_attribute(
      'name -> "group"))
    val groupsWithServers = client ! ModelNode.composite(nodes) map {
      case Response(Success, result) => {
        val stepResults = for {
          (step, node) <- result
          stepResult <- node.get("result")
        } yield stepResult
        for {
          stepResult <- stepResults
          server <- stepResult.values
          group <- server.get("result")
          name <- group.asString
        } yield name
      }
      case Response(Failure, failure) => List()
    } getOrElse List()

    val readGroups = ModelNode() at root op 'read_children_names('child_type -> "server-group")
    val groupsToRemove = stringValuesFromResultList(readGroups) diff groupsWithServers.toList

    // delete server groups
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
