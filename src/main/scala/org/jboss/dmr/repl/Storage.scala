package org.jboss.dmr.repl

import scala.util.Try
import java.io.{FileInputStream, FileOutputStream, File}
import org.jboss.dmr.{ModelNode => JavaModelNode}
import org.jboss.dmr.scala._

/** Provides an implicit conversion to call `save()` on [[org.jboss.dmr.scala.ModelNode]]s */
object Storage {
  val defaultStorage = new Storage

  class Adapter(val node: ModelNode) {
    def save(name: String = defaultStorage.autoName): Try[String] = defaultStorage.save(node)
  }

  implicit def saveModelNodes(node: ModelNode) = new Adapter(node)
}

/** Provides methods to save, load and remove model nodes on the file system */
class Storage(val basedir: File = new File(System.getProperty("user.home") + "/.dmr-repl")) {

  basedir.mkdirs()

  def save(node: ModelNode, name: String = autoName): Try[String] = {
    val abs = basedir.getAbsolutePath + "/" + name
    Try {
      node.underlying.writeBase64(new FileOutputStream(abs))
      abs
    }
  }

  def load(name: String): Try[ModelNode] = Try {
    new ComplexModelNode(JavaModelNode.fromBase64(new FileInputStream(fullName(name))))
  }

  def remove(name: String): Try[ModelNode] = load(name) map {
    (node) => {
      new File(fullName(name)).delete()
      node
    }
  }

  def ls() = basedir.listFiles().map(_.getName)

  private def autoName: String = "node_" + basedir.listFiles().size

  private def fullName(name: String) = if (name.startsWith(basedir.getAbsolutePath)) name else basedir + "/" + name
}
