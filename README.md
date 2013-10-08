# DMR.repl

A REPL shell for managing Wildfly server instances. Build on [DMR.scala](https://github.com/hpehl/dmr.scala) SBT and the Scala REPL at it's core.

## Execute DMR Operations

```scala
val client = connect()
val node = ModelNode() at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") op 'read_resource

// execute sync
(client ! node) match {
  case Some(response) => response match {
    case ModelNode(Response.Success, result) => println(s"Successful DMR operation: $result")
    case ModelNode(Response.Failed, failure) => println(s"DMR operation failed: $failure")
    case _ => println("Undefined result")
  }
  case None => println("Error reading response")
}

// execute async
import scala.util.{Success, Failure}

(client ? node).onComplete {
  case Success(response) => response match {
    case ModelNode(Response.Success, result) => println(s"Successful DMR operation: $result")
    case ModelNode(Response.Failed, failure) => println(s"DMR operation failed: $failure")
    case _ => println("Undefined result")
  }
  case Failure(ex) => println(s"DMR operation failed: $ex")
}
```

## Local Storage

```scala
val node = ModelNode() at ("core-service" -> "platform-mbean") / ("type" -> "runtime") op 'read_resource(
  'attributes_only -> true,
  'include_runtime -> false,
  'recursive_depth -> 3,
  'custom_parameter -> "custom-value"
)
storage.save(node, "mbean")

// later on
val node = storage.load("mbean").get
```
