package org.jboss.dmr.repl.samples

import scala.concurrent.duration._
import org.jboss.dmr.scala._
import org.jboss.dmr.repl._
import org.jboss.dmr.repl.Response._

/** Returns the uptime for the standalone server or for all servers in domain mode */
class Uptime extends Script[Map[String, Duration]] with SampleHelpers[Map[String, Duration]] {
  def code = {
    val standalone = client ! (ModelNode() at root op 'read_attribute('name -> "process-type")) map {
      case Response(Success, result) => result.asString.get == "Server"
      case Response(Failure, failure) => throw new ScriptException(failure)
    }

    standalone match {
      case util.Success(true) => {
        // standalone mode
        val node = ModelNode() at ("core-service" -> "platform-mbean") / ("type" -> "runtime") op 'read_attribute('name -> "uptime")
        client ! node map {
          case Response(Success, result) => Map("standalone" -> result.asLong.get.millis)
          case Response(Failure, failure) => throw new ScriptException(failure)
        }
      }
      case util.Success(false) => {
        // domain mode: read topology first
        val hosts = stringValues(ModelNode() at root op 'read_children_names('child_type -> "host"))
        val topology = for {
          host <- hosts
          servers = stringValues(ModelNode() at ("host" -> host) op 'read_children_names('child_type -> "server"))
          server <- servers
        } yield (host, server)

        // map topology to model node ops
        val nodes = topology.map {
          case (host, server) => {
            ModelNode()
              .at(("host" -> host) / ("server" -> server) / ("core-service" -> "platform-mbean") / ("type" -> "runtime"))
              .op('read_attribute('name -> "uptime"))
          }
        }
        client ! ModelNode.composite(nodes) map {
          case Composite(Success, steps) => {
            val durations = for {
              step <- steps
              uptime <- step.asLong
            } yield uptime.millis
            topology.map(_.toString()).zip(durations).toMap
          }
          case Composite(Failure, failure) => throw new ScriptException(failure.head)
        }
      }
      case util.Failure(ex) => throw new ScriptException(s"Cannot read execution mode: $ex")
    }
  }
}
