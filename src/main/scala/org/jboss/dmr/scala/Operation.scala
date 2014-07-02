package org.jboss.dmr.scala

import org.jboss.dmr.scala.Operation.Parameter

/** Holds a type alias for the parameters */
object Operation {

  type Parameter[Any] = (Symbol, Any)

  def apply(name: Symbol) = new Operation(name)
}

/**
 * An operation for a model node containing optional parameters. The name of the operation and its parameters are
 * specified as [[scala.Symbol]]s. Please use the shortcut `'foo` to create a symbol named "foo".
 *
 * However when creating a symbol in such a way the symbol must not contain "-". As most DMR operations and many
 * parameters do contain "-", all "_" in a symbol will be replaced with "-":
 * {{{
 * val readResourceOp = new Operation('read_resource)('include_runtime -> true)
 * // is exactly the same as
 * val readResourceOp = new Operation(Symbol("read-resource"))(Symbol("include-runtime") -> true)
 * }}}
 *
 * @param n The name of the operator as symbol.
 */
class Operation(val n: Symbol) {

  val name = Symbol(n.name.replace('_', '-'))
  var params = List.empty[Parameter[_]]

  /**
   * Adds a list of parameters to this operation
   *
   * @param params the parameters as name / value tuples
   * @return this operation containing the specified parameters
   */
  def apply(params: Parameter[Any]*): Operation = {
    this.params = params.map {
      case (symbol, value) => Symbol(symbol.name.replace('_', '-')) -> value
    }.toList
    this
  }

  override def toString = s"Operation($name,$params)"
}
