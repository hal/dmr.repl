package org.jboss.dmr.repl

import scala.Some
import scala.concurrent.{Promise, Future}
import scala.util.Try
import scala.util.Failure
import java.net.InetAddress
import javax.security.auth.callback._
import javax.security.sasl.RealmCallback
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.dmr.scala._

/** Provides an implicit default client which is used by scripts */
object Client {
  implicit val defaultClient = new Client

  def connect(host: String = "127.0.0.1", port: Int = 9999, username: String = null, password: String = null): Client =
    defaultClient.connect(host, port, username, password)

  def close() = defaultClient.close()
}

/** Client for making DMR calls */
class Client {
  private[repl] var connection: Option[ModelControllerClient] = None

  def connect(host: String = "127.0.0.1", port: Int = 9999, username: String = null, password: String = null): Client = {
    if (connection.isEmpty) {
      val handler = if (username != null && password != null) Some(new AuthHandler(username, password)) else None
      connection = Some(ModelControllerClient.Factory.create(InetAddress.getByName(host), port, handler.orNull))
    }
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

private class AuthHandler(username: String, password: String) extends CallbackHandler {
  def handle(callbacks: Array[Callback]) {
    for (callback <- callbacks) {
      callback match {
        case nc: NameCallback => nc.setName(username)
        case pc: PasswordCallback => pc.setPassword(password.toCharArray)
        case rc: RealmCallback => rc.setText(rc.getDefaultText)
        case other => throw new UnsupportedCallbackException(other)
      }
    }
  }
}

case class ClientException(msg: String) extends RuntimeException(msg)