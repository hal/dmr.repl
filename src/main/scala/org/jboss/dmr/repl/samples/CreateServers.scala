package org.jboss.dmr.repl.samples

import org.jboss.dmr.scala._
import org.jboss.dmr.repl._
import org.jboss.dmr.repl.Response.{Success, Failure}

/*
for {
  index <- 1 to 5
  group = if (index % 2 == 0) "main-server-group" else "new-server-group"
  name = s"server-$index"
} yield Server("master", group, name)
*/
case class Server(host: String, group: String, name: String)

class CreateServers(servers: Seq[Server], portOffset: Int = 100) extends Script[String] {
  val check = "\\u2713"

  override def code = {
    // check host: if host does not exists skip server
    // check group: if group does not exist, create with defaults
    // check server: if already exist skip with warning
    // create server asny
    // wait for all servers to be created
    val node = ModelNode() at root op 'read_resource
    client ! node map {
      case Response(Success, result) => "All servers created"
      case Response(Failure, failure) => throw new ScriptException(failure)
    }
  }
}
