# DMR.repl

A REPL shell for managing WildFly server instances. Build on [DMR.scala](https://github.com/hal/dmr.scala), SBT and
the Scala REPL at it's core.

## Connect

To make use of DMR.repl, first thing you'll need is a client for a WildFly server.

```scala
import org.jboss.dmr.repl.Client._

// Creates a client and connects to 127.0.0.1:9999
val client  = connect()

// Connects to specified host:port
val client = connect("homer", 9876)
```

## Execute DMR Operations

To execute an operation you can choose between `!` for synchronous and `?` for asynchronous execution. The
synchronous method returns with a `Try[ModelNode]`, the asynchronous with a `Future[ModelNode]`.

### Synchronous

```scala
import org.jboss.dmr.repl.Response.{Success, Failure}
val node = ModelNode() at ("subsystem" -> "datasources") op 'read_resource

(client ! node) map {
  case Response(Success, result) => ...
  case Response(Failure, failure) => ...
}
```

### Asynchronous

```scala
import scala.util.{Success, Failure}
val node = ModelNode() at ("subsystem" -> "datasources") op 'read_resource

(client ? node).onComplete {
  case Success(response) => ...
  case Failure(ex) => ...
}
```

When executing DMR operations, you can make use of some handy extractors which will return the actual payload of a DMR
response / composite response. If you execute a DMR operation like this

```scala
val node = ModelNode() at ("subsystem" -> "datasources") op 'read_resource
val response = client ! node getOrElse ModelNode.Undefined
```

`response` will contain the full response:

```
{
    "outcome" => "success",
    "result" => {
        "data-source" => {"ExampleDS" => undefined},
        "jdbc-driver" => {"h2" => undefined},
        "xa-data-source" => undefined
    }
}
```

If you're just interested in the result, use

```scala
val Response(Success, result) = response
```

which will assign a model node to `result` containing just the payload:

```
{
    "data-source" => {"ExampleDS" => undefined},
    "jdbc-driver" => {"h2" => undefined},
    "xa-data-source" => undefined
}
```

The same applies to composite operations:

```scala
val d1 = ModelNode() at ("deployment" -> "foo.war") op 'read_resource
val d2 = ModelNode() at ("deployment" -> "bar.war") op 'read_resource
val comp = ModelNode.composite(d1, d2)
val response = client ! comp getOrElse ModelNode.Undefined
```

Again `response` will contain the full response with the all steps and nested responses:

```
{
    "outcome" => "success",
    "result" => {
        "step-1" => {
            "outcome" => "success",
            "result" => {
                "name" => "foo.war",
                ...
            }
        },
        "step-2" => {
            "outcome" => "success",
            "result" => {
                "name" => "bar.war",
                ...
            }
        }
    }
}
```

To extract just the nested result nodes, use the `Composite` extractor:

```scala
val Composite(Success, steps) = response
```

which will give you a `List[ModelNode]`:

```scala
List(
{
    "name" => "foo.war",
    ...
},
{
    "name" => "bar.war",
    ...
})

```

## DMR Scripts

If you have a more advanced use cases or want to chain several operations, scripts are the way to go. To write a script
create a subclass of `Script` and override the `code` method. Scripts carry a type parameter for the expected result.
Furthermore they rely on an implicit client instance which is brought into scope by the clients companion object. So
please make sure you `import org.jboss.dmr.repl.Client._` when you run your script.

```scala
import scala.concurrent.duration._
import org.jboss.dmr.scala._
import org.jboss.dmr.repl._
import org.jboss.dmr.repl.Response._

/** Returns the uptime for the standalone server */
class Uptime extends Script[Duration] {
  def code = {
    val node = ModelNode() at ("core-service" -> "platform-mbean") / ("type" -> "runtime") op 'read_attribute('name -> "uptime")
    client ! node map {
      case Response(Success, result) => result.asLong.get.millis
      case Response(Failure, failure) => throw new ScriptException(failure)
    }
  }
}
```

Execute a script using its run method:

```scala
import org.jboss.dmr.repl.Client._

val uptime = new Uptime().run
```

Please see the [samples](src/main/scala/org/jboss/dmr/repl/samples) package for more advanced
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
