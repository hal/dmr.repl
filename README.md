# DMR.repl

A REPL shell for managing WildFly server instances. Build on [DMR.scala](https://github.com/hpehl/dmr.scala), SBT and
the Scala REPL at it's core.

## Connect

Use the following code snippet to create a client for a WildFly server.

```scala
import org.jboss.dmr.repl.Client._

// Connects to 127.0.0.1:9999
val client  = connect()

// Connects to specified host:port
val client = connect("homer", 9876)
```

Please note that you will receive a client no matter whether the WildFly server is running or not. Only if an
operation is executed, a connection is established.

## Execute DMR Operations

To execute an operation you can choose between `!` for synchronous and `?` for asynchronous execution. The
synchronous method returns with a `Try[ModelNode]`, the asynchronous with a `Future[ModelNode]`.

```scala
val node = ModelNode() at ("subsystem" -> "datasources") op 'read_resource

import org.jboss.dmr.repl.Response
import org.jboss.dmr.repl.Response.{Success, Failure}
(client ! node) map {
  case Response(Success, result) => ...
  case Response(Failure, failure) => ...
}

import scala.util.{Success, Failure}
(client ? node).onComplete {
  case Success(response) => ...
  case Failure(ex) => ...
}
```

## DMR Scripts

If you have a more advanced use cases or want to chain several operations, scripts are the way to go. To write a script
create a subclass of `Script` and override the `code` method. Scripts carry a type parameter for the expected result.
Furthermore they rely on an implicit client instance which is brought into scope by the clients companion object. So
please make sure you `import org.jboss.dmr.repl.Client._` when you run your script.

```scala
import org.jboss.dmr.scala._
import org.jboss.dmr.repl._
import org.jboss.dmr.repl.Response.{Success, Failure}

class About extends Script[ModelNode] {
  def code = {
    val node = ModelNode() at root op 'read_resource
    client ! node map {
      case Response(Success, result) => result
      case Response(Failure, failure) => throw new ScriptException(failure)
    }
  }
}
```

Execute a script using its run method:

```scala
val script = new About()
val node = scripts.run()
```

Please see the [samples](dmr-repl/tree/master/src/main/scala/org/jboss/dmr/repl/samples) package for more advanced
samples.

## Local Storage

You can save and load model nodes to and from the local file system. By default they are written as base64
to `~/.sbt/node_<counter>`, but you can choose another folder and/or filename.

```scala
val node = ModelNode() at ("core-service" -> "platform-mbean") / ("type" -> "runtime") op 'read_resource(
  'attributes_only -> true,
  'include_runtime -> false,
  'recursive_depth -> 3,
  'custom_parameter -> "custom-value"
)

// Uses an implicit conversion and the default folder and filename
node.save()

// Uses a storage using the folder 'nodes' in the current folder and saves the node as 'mbean'
val storage = new Storage(new java.io.File("nodes"))
storage.save(node, "mbean")

// load by name
val copy = storage.load("mbean")

// get rid of the saved node
storage.remove("mbean")
```
