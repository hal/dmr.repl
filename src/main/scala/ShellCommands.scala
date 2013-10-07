import java.net.InetAddress
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.dmr.{ModelNode => JavaModelNode}
import org.jboss.dmr.scala._
import org.jboss.dmr.scala.ModelNode

/**
 * Collection of utilities to enrich the scala shell
 */
object ShellCommands {

  import java.io.ByteArrayInputStream

  val storageDelegate = new Storage()

  def connect(host: String = "127.0.0.1", port: Int = 9999): ControllerClient = {
    val client = createClient
    client.connect(host, port)
    client
  }

  def createClient = new ControllerClient

  def storage() = storageDelegate

  def fromBase64(base64: String): Option[JavaModelNode] = {
    try {
      Some(
        JavaModelNode.fromBase64(
          new ByteArrayInputStream(base64.getBytes)
        )
      )
    } catch {
      case ex: java.lang.Throwable => None
    }
  }
}

/**
 * Wraps the ModelControllerClient in a scala & shell friendly way
 */
class ControllerClient() {

  private var delegate: Option[ModelControllerClient] = None

  def connect(host: String = "127.0.0.1", port: Int = 9999) {
    delegate = Some(
      ModelControllerClient.Factory.create(InetAddress.getByName(host), port)
    )
  }

  def close() = {
    for (con <- delegate) {
      con.close()
      delegate = None
    }
  }

  def execute(operation: ModelNode): Option[ModelNode] = {
    val result = for {
      con <- delegate
      result = con.execute(operation.underlying)
    } yield result
    result match {
      case Some(jn) => Some(new ComplexModelNode(jn))
      case None => None
    }
  }
}

/**
 * Utility to store and load model operations on the file system
 */
class Storage {

  import java.io._
  import scala.collection.mutable.ListBuffer

  private val basedir = new File(System.getProperty("user.home") + "/.dmr-repl")

  def save(op: ModelNode, name: String = autoName()): String = {
    basedir.mkdirs()
    val outName = basedir.getAbsolutePath + "/" + name
    op.underlying.writeBase64(new FileOutputStream(outName))
    outName
  }

  def remove(name: String): Option[ModelNode] = {
    load(name) match {
      case None => None
      case Some(op) => {
        val inName = basedir.getAbsolutePath + "/" + name
        new File(inName).delete()
        Some(op)
      }
    }
  }

  def load(name: String): Option[ModelNode] = {
    basedir.mkdirs()
    val inName = basedir.getAbsolutePath + "/" + name
    try {
      Some(new ComplexModelNode(JavaModelNode.fromBase64(new FileInputStream(inName))))
    } catch {
      case ex: java.lang.Throwable => None
    }
  }

  def contents() = {
    val files = new ListBuffer[String]
    basedir.mkdirs()
    for (file <- basedir.listFiles()) {
      files += file.getName
    }
    val fileNames = files.toList

    for (file <- fileNames) {
      println(file)
    }
  }

  private def autoName(): String = {
    basedir.mkdirs()
    var count = 0
    for (file <- basedir.listFiles()) {
      count += 1
    }
    "op_" + count
  }
}
