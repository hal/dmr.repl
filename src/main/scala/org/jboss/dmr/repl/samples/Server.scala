package org.jboss.dmr.repl.samples

/** Provides a factory method to quickly generate some server instances */
object Servers {
  def apply(size: Int) = {
    for {
      index <- 1 to size
      group = if (index % 2 == 0) "main-server-group" else if (index % 3 == 0) "foo-server-group" else "bar-server-group"
      name = s"server-$index"
    } yield Server(group, "master", name)
  }
}

/** Simple data holder for a server instance */
case class Server(group: String, host: String, name: String)
