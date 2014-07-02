[![Build Status](https://travis-ci.org/hal/dmr.repl.png)](https://travis-ci.org/hal/dmr.repl)

# DMR.repl

A REPL shell for managing WildFly server instances. Build on a Scala wrapper around 
[DMR]((https://docs.jboss.org/author/display/WFLY8/Detyped+management+and+the+jboss-dmr+library)), SBT and
the Scala REPL at it's core.

Features:

* DSL for creating DMR operations
* Interact with model nodes in a more natural way

For an introduction to DMR please refer to the [WildFly Wiki](https://docs.jboss.org/author/display/WFLY8/Detyped+management+and+the+jboss-dmr+library).

## Working with model nodes

Please make sure to import the following packages in order to bring the necessary implicit conversions into scope and
to save you some keystrokes.

```scala
import org.jboss.dmr.scala._
import org.jboss.dmr.scala.ModelNode
```

## Creating model nodes

Use the factory methods in `org.jboss.dmr.scala.ModelNode` to create model nodes:

```scala
// creates an empty model node
val node = ModelNode()

// creates a new model node holding a simple value
val node = ModelNode(42)

// creates a new model node with structure
val node = ModelNode(
  "flag" -> true,
  "hello" -> "world",
  "answer" -> 42,
  "child" -> ModelNode(
    "inner-a" -> 123,
    "inner-b" -> "test",
    "deep-inside" -> ModelNode("foo" -> "bar"),
    "deep-list" -> List(
      ModelNode("one" -> 1),
      ModelNode("two" -> 2),
      ModelNode("three" -> 3)
    ),
    "value-list" -> List(
      ModelNode(1),
      ModelNode(2),
      ModelNode(3)
    )
  )
)
```

## Addressing and operations

You can use a DSL like API to set the address and operation for a model node. To describe the "read-resource" operation
on "/subsystem=datasources/data-source=ExampleDS" use the following code:

```scala
ModelNode() at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") op 'read_resource(
  'include_runtime -> false,
  'recursive_depth -> 2
)
```

Addresses can be written down as `(String, String)` tuples separated by "/". Operations are specified using
`Symbol`s and an optional list of parameters. Each parameter is made up of another `Symbol` and a value. Using symbols
makes the DSL both more readable and extentable.

However there's one drawback in using symbols: when using the short form `'someSymbol` characters like "-" are not allowed. `'read-resource` is therefore an illegal symbol. As most DMR operations and many parameters do contain "-", all
underscores will be replaced with dashes:

```scala
ModelNode() op 'read_resource('include_runtime -> true)
// is exactly the same as
ModelNode() op Symbol("read-resource")(Symbol("include-runtime") -> true)
```

Here are some more examples using addresses and operations:

```scala
// root is a constant for an empty address
ModelNode() at root op 'read_resource
ModelNode() at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") op 'disable

// parameters are specified as pairs (Symbol -> Any)
ModelNode() at ("core-service" -> "platform-mbean") / ("type" -> "runtime") op 'read_resource(
  'attributes_only -> true,
  'include_runtime -> false,
  'recursive_depth -> 3,
  'custom_parameter -> "custom-value"
)

// unsupported parameter types will throw an IllegalArgumentException
ModelNode() at root op 'read_resource('proxies -> Console.out)
```

## Reading Nodes

Reading values from a model node follows the sementics of a `Map[String, ModelNode]`, but instead of a string you have to
provide a `Path` as key. Thanks to an implicit conversion expressions like `"a" / "b" / "c"` are automatically converted
to a path.

```scala
val node = ModelNode(
  "flag" -> true,
  "hello" -> "world",
  "answer" -> 42,
  "level0" -> ModelNode(
    "level1" -> ModelNode(
      "level2" -> ModelNode(
        "level3" -> ModelNode(
          "level4" -> ModelNode("foo" -> "bar")
        )
      )
    )
  )
)

val flag = node("flag")
val boom = node("gag") // throws a NoSuchelementException
val hello = node.get("hello") // returns an Option[ModelNode]
val x = node.getOrElse("nope", ModelNode("y"))
val check = node.contains("level0" / "level1" / "level2" / "level3")
val level3 = node("level0" / "level1" / "level2" / "level3")
val level2 = for {
  l0 <- node.get("level0")
  l1 <- l0.get("level1")
  l2 <- l1.get("level2")
} yield l2
```

Since the result of `node.get("a" / "b" / "c")` is `Option[ModelNode]`, reading nested model nodes is safe even if
some children in the path do not exist. In this case `None` wil be returned:

```scala
val nope = node.get("level0" / "oops" / "level2" / "level3")
```

## Reading Values

You can use the folowing methods to read values from model nodes:

- `ModelNode.asBoolean`
- `ModelNode.asInt`
- `ModelNode.asLong`
- `ModelNode.asBigInt`
- `ModelNode.asDouble`
- `ModelNode.asString`

These methods return `Option` instances of the relevant type. This is because not all conversions make sense on all kind of
model nodes:

```scala
val node = ModelNode(
  "flag" -> true,
  "child" -> ModelNode(
    "size" -> 0
  )
)

val nonsense = node("child").asDouble // None
```

## Writing

Simple values can be set using `node("foo") = "bar"`. If "foo" doesn't exist it will be created and updated
otherwise. As an alternative you can use the `+=` operator, which comes in handy if you want to add multiple
key / value pairs:

```scala
val node = ModelNode()

node("foo") = "bar"
node += ("foo" -> "bar")

node += (
  "flag" -> true,
  "hello" -> "world",
  "answer" -> 42,
  "child" -> ModelNode(
    "inner-a" -> 123,
    "inner-b" -> "test",
    "deep-inside" -> ModelNode(
      "foo" -> "bar"
    ),
    "deep-list" -> List(
      ModelNode("one" -> 1),
      ModelNode("two" -> 2),
      ModelNode("three" -> 3)
    )
  )
)
```

Reading and writing can also be combined in one call:

```scala
node("child" / "deep-inside") += ("foo" -> "xyz")
```

## Collection Operations

Since `ModelNode` mixes in `Traversable[(String, ModelNode)]` you can use all those nifty collection methods like
`foreach`, `map` or `filter`:

```scala
val node = ModelNode(
  "flag" -> true,
  "hello" -> "world",
  "answer" -> 42
)

// turn all keys to upper case
val shout = node.map(kv => kv._1.toUpperCase -> kv._2)

// filter for nodes
val aa = node.filter(_._1 contains "a")
val n42 = node.filter(_._2 == ModelNode(42))

// combine nodes
val node2 = node ++ ModelNode("abc" -> 1)
```

Please note that these kind of methods only traverse over the direct children of a model node. If you want to traverse
over all children in a deeply nested model node use the `inOrder` method which gives you a list
of `(String, ModelNode)` tuples:

```scala
val node = ModelNode(
  "flag" -> true,
  "answer" -> 42,
  "child" -> ModelNode(
    "inner-a" -> 123,
    "inner-b" -> "test",
    "deep-inside" -> ModelNode("foo" -> "bar"),
    "deep-list" -> List(
      ModelNode("one" -> 1),
      ModelNode("two" -> 2)
    ),
    "value-list" -> List(
      ModelNode(1),
      ModelNode(2)
    )
  )
)

node.inOrder map { tpl => tpl._1 }
// will result in List(flag, answer, child, inner-a, inner-b, deep-inside, foo, deep-list, one, two, value-list)
```

## Pattern Matching / Extractor

Model nodes support pattern matching / extractor against their type:

```scala
val node = ModelNode(
  "flag" -> true,
  "hello" -> "world",
  "answer" -> 42,
  "one" -> 1,
  "two" -> 2
)

import org.jboss.dmr.ModelType._
for ((key, node) <- node) node match {
  case ModelNode(INT) => println(s"$key is an integer: $node")
  case ModelNode(t) => println(s"$key is not an integer, but $t")
}

val (key, firstNode) = node.head
val ModelNode(booleanType) = firstNode
// booleanType == org.jboss.dmr.ModelType.BOOLEAN

val integerNodes = for {
  (key, node) <- node
  ModelNode(t) = node
  if t == INT
} yield node
// results in List(42, 1, 2)
```

## Composites

A composite operation is setup using the `ModelNode.composite(n: ModelNode, xn: ModelNode*)` factory method:

```scala
ModelNode.composite(
  ModelNode() at ("core-service" -> "management") / ("access" -> "authorization") op 'read_resource(
    'recursive_depth -> 2),
  ModelNode() at ("core-service" -> "management") / ("access" -> "authorization") op 'read_children_names(
    'name -> "role-mapping"),
  ModelNode() at ("subsystem" -> "mail") / ("mail-session" -> "*") op 'read_resource_description,
  ModelNode() at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") op 'disable ,
  ModelNode() at ("core-service" -> "platform-mbean") / ("type" -> "runtime") op 'read_attribute(
    'name -> "start-time")
)
```

## Execute an operation

To execute DMR operations against a running WildFly instance use [DMR.repl](https://github.com/hal/dmr.repl).
The `Response` object has an extractor and constants to parse the DMR response using pattern matching. The pattern
matching variables `result` and `failure` are both model nodes containing the response payload or the wrapped error
description:

```scala
val client = connect()
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

# REPL Shell

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
import scala.util.Try
import org.jboss.dmr.scala._
import org.jboss.dmr.repl._
import org.jboss.dmr.repl.Response._

/** Returns the uptime for the standalone server */
class Uptime extends Script[Duration] {
  def code: Try[Duration] = {
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

connect()
val uptime = new Uptime().run()
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
