
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.dmr.ModelNode

object Fluent extends App {

  val op = new ModelNode()
  op.get(OP).set("")

  println(op)
}
