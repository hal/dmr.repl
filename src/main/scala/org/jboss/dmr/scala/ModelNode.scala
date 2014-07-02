package org.jboss.dmr.scala

import org.jboss.dmr.ModelType._
import org.jboss.dmr.scala.ModelNode.NodeTuple
import org.jboss.dmr.{ModelType, ModelNode => JavaModelNode}
import scala.collection.JavaConversions._
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.ListBuffer

/** Factory for [[org.jboss.dmr.scala.ModelNode]] */
object ModelNode {

  type NodeTuple = (String, ModelNode)

  object Undefined extends ComplexModelNode

  /** Creates a new model node holding the given value */
  def apply(value: AnyVal): ModelNode = {
    val jvalue = value match {
      case boolean: Boolean => new JavaModelNode(boolean)
      case int: Int => new JavaModelNode(int)
      case long: Long => new JavaModelNode(long)
      case float: Float => new JavaModelNode(float)
      case double: Double => new JavaModelNode(double)
      case _ => new JavaModelNode()
    }
    new ValueModelNode(jvalue)
  }

  /** Creates a new model node holding the given string */
  def apply(value: String): ModelNode = new ValueModelNode(new JavaModelNode(value))

  /**
   * Creates a new model node with the specified key / value pairs. Use this method to create a hierarchy of model
   * nodes:
   * {{{
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
   * @param kvs the key / value pairs
   */
  def apply(kvs: (String, Any)*): ModelNode = new ComplexModelNode() += (kvs: _*)

  /**
   * Creates a new composite containing the specified model nodes
   *
   * @param n the first model node
   * @param xn additional model nodes
   */
  def composite(n: ModelNode, xn: ModelNode*): ModelNode = composite(List(n) ++ xn)

  /**
   * Creates a new composite containing the specified model nodes
   *
   * @param nodes the model nodes
   */
  def composite(nodes: Traversable[ModelNode]): ModelNode = {
    val node = new ComplexModelNode() op 'composite
    node("steps") = nodes
    node
  }

  implicit def canBuildFrom: CanBuildFrom[ModelNode, NodeTuple, ModelNode] =
    new CanBuildFrom[ModelNode, NodeTuple, ModelNode] {
      def apply(): collection.mutable.Builder[NodeTuple, ModelNode] = newBuilder

      def apply(from: ModelNode): collection.mutable.Builder[NodeTuple, ModelNode] = newBuilder
    }

  def newBuilder: collection.mutable.Builder[NodeTuple, ModelNode] = {
    new ListBuffer().mapResult(kvs => new ComplexModelNode() += (kvs: _*))
  }

  /** Extractor based on the model nodes type */
  def unapply(node: ModelNode): Option[ModelType] = Some(node.underlying.getType)
}

/**
 * A Scala wrapper around a `org.jboss.dmr.ModelNode` providing methods to interact with model nodes in a more
 * natural way.
 *
 * This class uses some of the semantics and methods of [[scala.collection.Map]] while mixing
 * in [[scala.collection.TraversableLike]] which turns a model node into a collection of [[( String, M o d e l N o d e )]] tuple.
 *
 * @param javaModelNode the underlying Java `org.jboss.dmr.ModelNode`
 */
abstract class ModelNode(javaModelNode: JavaModelNode)
  extends Traversable[NodeTuple]
  with TraversableLike[NodeTuple, ModelNode]
  with ValueConversions {

  /** Returns the underlying Java mode node */
  def underlying = javaModelNode

  //---------------------------------------- DSL methods

  /**
   * Sets the address for this model node. An address can be specified using `(String, String)` tuple separated
   * by "/". Thus an expression of  `("subsystem" -> "datasources") / ("data-source" -> "ExampleDS")` will be
   * implicitly converted to an address.
   *
   * @param address the address
   * @return this model node with the address st
   */
  def at(address: Address): ModelNode

  /**
   * Sets the operation for this model node. The operation can be specified as a [[scala.Symbol]] which will
   * be implicitly converted to an operation.
   *
   * @param operation the operation.
   * @return this model node with the operation set
   */
  def op(operation: Operation): ModelNode

  //---------------------------------------- read only methods

  /**
   * Returns the model node associated with a path, or throws a `NoSuchElementException` if the path is
   * not contained in the model node.
   */
  def apply(path: Path): ModelNode = getOrElse(path, throw new NoSuchElementException)

  /** Optionally returns the value associated with a path. */
  def get(path: Path): Option[ModelNode] = {
    if (underlying.has(path.elements.head)) {
      val jchild = underlying.get(path.elements.head)
      val child = fromJavaNode(jchild)
      path.elements.tail match {
        case Nil => Some(child)
        case _ => child.get(Path(path.elements.tail))
      }
    } else None
  }

  /**
   * Returns the model node associated with a path, or a default value if the path is not contained in the
   * model node.
   *
   * @param path the path
   * @param default a computation that yields a default value in case no binding for `path` is found in the model node.
   * @return the value associated with `path` if it exists, otherwise the result of the `default` computation.
   */
  def getOrElse(path: Path, default: => ModelNode): ModelNode = get(path) match {
    case Some(node) => node
    case None => default
  }

  /** Tests whether this model node contains a binding for a path. */
  def contains(path: Path): Boolean = get(path) match {
    case Some(x) => true
    case None => false
  }

  /** Returns the keys for this model node */
  def keys: Iterable[String] = contents.map {
    case (key, _) => key
  }

  /** Returns the values for this model node */
  def values: Iterable[ModelNode] = underlying.getType match {
    case LIST => asList getOrElse Nil
    case _ => contents.map {
      case (_, value) => value
    }
  }

  //---------------------------------------- traversable methods

  override def foreach[U](f: (NodeTuple) => U): Unit = contents.foreach(f)

  override protected[this] def newBuilder: collection.mutable.Builder[NodeTuple, ModelNode] = ModelNode.newBuilder

  /** Traversals the model node tree using inorder */
  def inOrder: List[NodeTuple] = inOrder(underlying)

  private def inOrder(jnode: JavaModelNode): List[NodeTuple] = {
    jnode.getType match {
      case OBJECT => jnode.asList().flatMap(inOrder(_)).toList
      case LIST => List(propAsTuple(jnode))
      case PROPERTY =>
        val propValue: JavaModelNode = jnode.asProperty().getValue
        propValue.getType match {
          case OBJECT | LIST => List(propAsTuple(jnode)) ++ propValue.asList().flatMap(inOrder(_)).toList
          case _ => List(propAsTuple(jnode))
        }
      case _ => List()
    }
  }

  //---------------------------------------- update methods

  /**
   * Adds multiple key / value pairs to this model node:
   * {{{
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
   * @param kvs the key / value pairs
   */
  def +=(kvs: (String, Any)*): ModelNode = {
    kvs.foreach(tuple => this(tuple._1) = tuple._2)
    this
  }

  /**
   * Adds a given key/value pair. Supports the following types:
   * <ul>
   * <li> Boolean
   * <li> Int
   * <li> Long
   * <li> BigInt
   * <li> Float
   * <li> Double
   * <li> BigDecimal
   * <li> String
   * <li> ModelNode
   * <li> Traversable[ModelNode]
   * </ul>
   *
   * @param name the name
   * @param value the value
   * @throws IllegalArgumentException if the type is not supported
   */
  @throws[IllegalArgumentException]("if the type is not supported")
  def update(name: String, value: Any) {
    value match {
      case boolean: Boolean => underlying.get(name).set(boolean)
      case int: Int => underlying.get(name).set(int)
      case long: Long => underlying.get(name).set(long)
      case bigInt: BigInt => underlying.get(name).set(bigInt.underlying())
      case float: Float => underlying.get(name).set(float)
      case double: Double => underlying.get(name).set(double)
      case bigDecimal: BigDecimal => underlying.get(name).set(bigDecimal.underlying())
      case string: String => underlying.get(name).set(string)
      case node: ModelNode => underlying.get(name).set(node.underlying)
      case values: Traversable[_] =>
        try {
          // only collections of model nodes are supported!
          val nodes = values.asInstanceOf[Traversable[ModelNode]]
          val javaNodes = nodes.toList.map(_.underlying)
          javaModelNode.get(name).set(javaNodes)
        } catch {
          case e: ClassCastException => throw new IllegalArgumentException(
            s"""Illegal type parameter in type ${value.getClass.getName} for "$name".""")
        }
      case _ => throw new IllegalArgumentException( s"""Illegal type ${value.getClass.getName} for "$name".""")
    }
  }

  //---------------------------------------- helper methods

  private def contents: List[NodeTuple] = underlying.getType match {
    case OBJECT => underlying.asList().map(propAsTuple).toList
    case _ => List.empty
  }

  private def propAsTuple(jnode: JavaModelNode): NodeTuple = {
    val prop = jnode.asProperty()
    (prop.getName, fromJavaNode(prop.getValue))
  }

  private[scala] def fromJavaNode(jnode: JavaModelNode): ModelNode = jnode.getType match {
    case OBJECT => new ComplexModelNode(jnode)
    case PROPERTY => new ComplexModelNode(jnode.asObject()) // don't want to have properties in the scala API
    case _ => new ValueModelNode(jnode)
  }

  //---------------------------------------- object methods

  /** Delegates to `underlying.hashCode()` */
  override def hashCode(): Int = underlying.hashCode()

  /** Delegates to `underlying.equals()` if `obj` is also a model node, returns false otherwise */
  override def equals(obj: Any): Boolean = obj match {
    case node: ModelNode => underlying.equals(node.underlying)
    case _ => false
  }

  /** Delegates to `underlying.toString` */
  override def toString() = underlying.toString
}

/**
 * Implementation for complex model nodes.
 *
 * @param javaModelNode the underlying Java `org.jboss.dmr.ModelNode`
 */
class ComplexModelNode(javaModelNode: JavaModelNode = new JavaModelNode()) extends ModelNode(javaModelNode) {

  override def at(address: Address): ModelNode = {
    underlying.get("address").setEmptyList()
    address.tuple.foreach(tuple => underlying.get("address").add(tuple._1, tuple._2))
    this
  }

  override def op(operation: Operation): ModelNode = {
    underlying.get("operation").set(operation.name.name)
    operation.params.foreach(param => this(param._1.name) = param._2)
    this
  }
}

/**
 * Implementation for value model nodes. Contains empty implementations for [[org.jboss.dmr.scala.ModelNode# a t]] and
 * [[org.jboss.dmr.scala.ModelNode# o p]].
 *
 * @param javaModelNode the underlying Java `org.jboss.dmr.ModelNode`
 */
class ValueModelNode(javaModelNode: JavaModelNode) extends ModelNode(javaModelNode) {

  /** Safe nop - returns this value model unmodified */
  def at(address: Address): ModelNode = this

  /** Safe nop - returns this value model unmodified */
  def op(operation: Operation): ModelNode = this
}