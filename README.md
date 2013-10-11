# DMR.repl

A REPL shell for managing Wildfly server instances. Build on [DMR.scala](https://github.com/hpehl/dmr.scala) SBT and the Scala REPL at it's core.

## Execute DMR Operations

```scala
val client = connect()
val node = ModelNode() at ("subsystem" -> "datasources") op 'read_resource

def processResponse(response: ModelNode): Unit = response match {
  case Response(Response.Success, result) => println(s"Success: $result")
  case Response(Response.Failure, failure) => println(s"Failed: $failure")
  case _ => println(s"Response not parsable: $response")
}

// execute sync
(client ! node) match {
  case Some(response) => processResponse(response)
  case None => println("Error reading response")
}

// execute async
import scala.util.{Success, Failure}
(client ? node).onComplete {
  case Success(response) => processResponse(response)
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
