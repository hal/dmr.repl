package org.jboss.dmr.repl.samples

import org.jboss.dmr.scala._
import org.jboss.dmr.repl._
import org.jboss.dmr.repl.Response._

case class Version(major: Int, minor: Int, micro: Int = 0)
case class Extension(name: String, version: Version)

/** Reads the installed extensions and returns them as a list of [[org.jboss.dmr.repl.samples.Extension]]s */
class ListExtensions extends Script[Traversable[Extension]]{
  def code = {
    val node = ModelNode() at root op 'read_children_resources(
      'child_type -> "extension",
      'recursive_depth -> 2
    )
    client ! node map {
      case Response(Success, result) => {
        for {
          (name, extension) <- result
          (_, subsystem) = extension("subsystem").head
          major = subsystem("management-major-version").asInt getOrElse -1
          micro = subsystem("management-micro-version").asInt getOrElse -1
          minor = subsystem("management-minor-version").asInt getOrElse -1
        } yield Extension(name, Version(major, minor, micro))
      }
      case Response(Failure, failure) => throw new ScriptException(failure)
    }
  }
}
