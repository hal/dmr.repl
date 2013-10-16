package org.jboss.dmr.repl

import scala.concurrent.{Promise, Future}
import scala.util.{Failure, Try}
import java.net.InetAddress
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.dmr.scala._

object Client {
  implicit val defaultClient = new Client

  def connect(host: String = "127.0.0.1", port: Int = 9999): Client = defaultClient.connect(host, port)

  def close() = defaultClient.close()
}

/** DMR client */
class Client {
  private[repl] var connection: Option[ModelControllerClient] = None

  def connect(host: String = "127.0.0.1", port: Int = 9999): Client = {
    if (connection.isEmpty)
      connection = Some(ModelControllerClient.Factory.create(InetAddress.getByName(host), port))
    this
  }

  def close() = {
    for (con <- connection) {
      con.close()
      connection = None
    }
  }

  def !(operation: ModelNode): Try[ModelNode] = connection match {
    case Some(con) => Try(con.execute(operation.underlying)).map(new ComplexModelNode(_))
    case None => Failure(new ClientException("No connection"))
  }

  def ?(operation: ModelNode): Future[ModelNode] = {
    val p = Promise[ModelNode]()
    connection match {
      case Some(con) => {
        Try {
          val async = con.executeAsync(operation.underlying, null)
          p.success(new ComplexModelNode(async.get()))
        }
      }
      case None => p.failure(new ClientException("No connection"))
    }
    p.future
  }
}

case class ClientException(msg: String) extends RuntimeException(msg)