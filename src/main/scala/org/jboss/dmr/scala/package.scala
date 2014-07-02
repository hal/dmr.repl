package org.jboss.dmr

/** Provides classes for dealing with the management model of a WildFly, JBoss or EAP instance.
  *
  * ==Getting started==
  * Please make sure to import the following packages in order to bring the necessary implicit conversions into scope
  * and to save you some keystrokes.
  * {{{
  * import org.jboss.dmr.scala._
  * import org.jboss.dmr.scala.ModelNode
  * }}}
  *
  * ==Creating model nodes==
  * The main class to use is [[org.jboss.dmr.scala.ModelNode]]. Its companion object acts as a factory for creating new
  * model node instances.
  * {{{
  * // creates an empty model node
  * val node = ModelNode()
  *
  * // creates a new model node holding a simple value
  * val node = ModelNode(42)
  *
  * // creates a new model node with structure
  * val node = ModelNode(
  * "flag" -> true,
  * "hello" -> "world",
  * "answer" -> 42,
  * "child" -> ModelNode(
  * "inner-a" -> 123,
  * "inner-b" -> "test",
  * "deep-inside" -> ModelNode("foo" -> "bar"),
  * "deep-list" -> List(
  * ModelNode("one" -> 1),
  * ModelNode("two" -> 2),
  * ModelNode("three" -> 3)
  * )
  * )
  * )
  * }}}
  *
  * ==Addressing and operations==
  * You can use a DSL like API to set the address and operation for a model node. To describe the "read-resource"
  * operation on "/subsystem=datasources/data-source=ExampleDS" use the following code:
  * {{{
  * ModelNode() at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") op 'read_resource(
  *   'include_runtime -> false,
  *   'recursive_depth -> 2
  * )
  * }}}
  *
  * Addresses can be written down as `(String, String)` tuples separated by "/". Operations are specified using
  * [[scala.Symbol]]s and an optional list of parameters. Each parameter is made up of another [[scala.Symbol]] and a
  * value. Using symbols makes the DSL both more readable and extentable.
  *
  * However there's one drawback in using symbols: they cannot contain characters like "-". `'read-resource` is
  * therefore an illegal symbol. As most DMR operations and many parameters do contain "-", this library will replace
  * all underscores in a symbol with dashes:
  * {{{
  * ModelNode() op 'read_resource('include_runtime -> true)
  * // is exactly the same as
  * ModelNode() op Symbol("read-resource")(Symbol("include-runtime") -> true)
  * }}}
  *
  * Here are some more examples using addresses and operations:
  * {{{
  * // root is a constant for an empty address
  * ModelNode() at root op 'read_resource
  * ModelNode() at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") op 'disable
  *
  * // parameters are specified as pairs (Symbol -> Any)
  * ModelNode() at ("core-service" -> "platform-mbean") / ("type" -> "runtime") op 'read_resource(
  *   'attributes_only -> true,
  *   'include_runtime -> false,
  *   'recursive_depth -> 3,
  *   'custom_parameter -> "custom-value"
  * )
  *
  * // unsupported parameter types will throw an IllegalArgumentException
  * ModelNode() at root op 'read_resource('proxies -> Console.out)
  * }}}
  *
  * ==Reading Nodes==
  * Reading values from a model node follows the sementics of a `Map[String, ModelNode]`, but instead of a string you
  * provide a [[org.jboss.dmr.scala.Path]] as key. Thanks to an implicit conversion expression like `"a" / "b" / "c"`
  * are automatically converted to a path.
  * {{{
  * val node = ModelNode(
  * "flag" -> true,
  * "hello" -> "world",
  * "answer" -> 42,
  * "level0" -> ModelNode(
  * "level1" -> ModelNode(
  * "level2" -> ModelNode(
  * "level3" -> ModelNode(
  * "level4" -> ModelNode("foo" -> "bar")
  * )
  * )
  * )
  * )
  * )
  *
  * val flag = node("flag")
  * val boom = node("gag") // throws a NoSuchelementException
  * val hello = node.get("hello") // returns an Option[ModelNode]
  * val x = node.getOrElse("nope", ModelNode("y"))
  * val check = node.contains("level0" / "level1" / "level2" / "level3")
  * val level3 = node("level0" / "level1" / "level2" / "level3")
  * val level2 = for {
  * l0 <- node.get("level0")
  * l1 <- l0.get("level1")
  * l2 <- l1.get("level2")
  * } yield l2
  * }}}
  *
  * Since the result of `node.get("a" / "b" / "c")` is `Option[ModelNode]`, reading nested model nodes is safe even
  * if some children in the path do not exist. In this case `None` wil be returned:
  * {{{
  * val nope = node.get("level0", "oops", "level2", "level3")
  * }}}
  *
  * ==Reading Values==
  * You can use the folowing methods to read values from model nodes:
  * <ul>
  * <li> `ModelNode.asBoolean`
  * <li> `ModelNode.asInt`
  * <li> `ModelNode.asLong`
  * <li> `ModelNode.asBigInt`
  * <li> `ModelNode.asDouble`
  * <li> `ModelNode.asString`
  * </ul>
  * These methods return `Option` instances of the relevant type. This is because not all methods make sense on all
  * kind of model nodes:
  * {{{
  * val node = ModelNode(
  * "flag" -> true,
  * "child" -> ModelNode(
  * "size" -> 0
  * )
  * )
  *
  * val nonsense = node("child").asDouble
  * }}}
  *
  *
  * ==Writing==
  * Simple values can be set using `node("foo") = "bar"`. If "foo" doesn't exist it will be created and updated
  * otherwise. As an alternative you can use the `+=` operator, which comes in handy if you want to add multiple
  * key / value pairs:
  * {{{
  * val node = ModelNode()
  *
  * node("foo") = "bar"
  * node += ("foo" -> "bar")
  *
  * node += (
  * "flag" -> true,
  * "hello" -> "world",
  * "answer" -> 42,
  * "child" -> ModelNode(
  * "inner-a" -> 123,
  * "inner-b" -> "test",
  * "deep-inside" -> ModelNode("foo" -> "bar"),
  * "deep-list" -> List(
  * ModelNode("one" -> 1),
  * ModelNode("two" -> 2),
  * ModelNode("three" -> 3)
  * )
  * )
  * )
  * }}}
  *
  * Reading and writing can also be combined in one call:
  * {{{
  * node("child" / "deep-inside") += ("foo" -> "xyz")
  * }}}
  *
  * ==Colection Operations==
  * Since [[org.jboss.dmr.scala.ModelNode]] mixes in [[scala.collection.Traversable[ ( S t r i n g, M o d e l N o d e )]]] you can use
  * all those nifty collection methods like `foreach`, `map` or `filter`
  * {{{
  * val node = ModelNode(
  * "flag" -> true,
  * "hello" -> "world",
  * "answer" -> 42
  * )
  *
  * // turn all keys to upper case
  * val shout = node.map(kv => kv._1.toUpperCase -> kv._2)
  *
  * // filter nodes
  * val aa = node.filter(_._1 contains "a")
  * val n42 = node.filter(_._2 == ModelNode(42))
  *
  * // combine nodes
  * val node2 = node ++ ModelNode("abc" -> 1)
  * }}}
  *
  * ==Composites==
  * A composite operation is setup using the `ModelNode.composite(n: ModelNode, xn: ModelNode*)` factory method:
  * {{{
  * ModelNode.composite(
  *   ModelNode.empty at ("core-service" -> "management") / ("access" -> "authorization") op 'read_resource(
  *     'recursive_depth -> 2),
  *   ModelNode.empty at ("core-service" -> "management") / ("access" -> "authorization") op 'read_children_names(
  *     'name -> "role-mapping"),
  *   ModelNode.empty at ("subsystem" -> "mail") / ("mail-session" -> "*") op 'read_resource_description,
  *   ModelNode.empty at ("subsystem" -> "datasources") / ("data-source" -> "ExampleDS") op 'disable ,
  *   ModelNode.empty at ("core-service" -> "platform-mbean") / ("type" -> "runtime") op 'read_attribute(
  *     'name -> "start-time")
  * )
  *
  *
  * ==Implicit Conversions==
  * This package contains the following implicit conversions which are used to support a DSL like API:
  *
  * <ul>
  * <li> `(String, String)` to [[org.jboss.dmr.scala.Address]]
  * <li> [[scala.Symbol]] to [[org.jboss.dmr.scala.Operation]]
  * <li> String to [[org.jboss.dmr.scala.Path]]
  * </ul>
  *
  */
package object scala {
  /** An empty address */
  val root = Address(List())

  implicit def tupleToAddress(tuple: (String, String)) = Address(List(tuple))

  implicit def symbolToOperation(name: Symbol) = Operation(name)

  implicit def stringToPath(element: String) = Path(List(element))
}
