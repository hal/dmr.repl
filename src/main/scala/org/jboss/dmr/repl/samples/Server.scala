package org.jboss.dmr.repl.samples

object Servers {
  def apply(size: Int) = {
    for {
      index <- 1 to size
      group = if (index % 2 == 0) "main-server-group" else if (index % 3 == 0) "foo-server-group" else "bar-server-group"
      name = s"server-$index"
    } yield Server(group, "master", name)
  }
}

case class Server(group: String, host: String, name: String)
