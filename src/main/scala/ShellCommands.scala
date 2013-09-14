import java.net.InetAddress
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.dmr.ModelNode

/**
 * Collection of utilities to enrich the scala shell
 */
object ShellCommands {
  def createClient = new ControllerClient
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
    for(con <- delegate) {
      con.close()
      delegate = None
    }
  }

  def execute(operation: ModelNode) : Option[ModelNode]  = {
    for {
      con <- delegate
    } yield delegate.get.execute(operation)
  }

}



