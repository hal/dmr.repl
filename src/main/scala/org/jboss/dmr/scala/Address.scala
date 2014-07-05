package org.jboss.dmr.scala

import scala.math.Ordered

/** Helper class for holding the address tuple for a model node */
case class Address(tuple: List[(String, String)]) extends Ordered[Address] {

  def /(address: Address) = Address(tuple ++ address.tuple)

  override def compare(that: Address): Int = this.toString compare that.toString

  override def toString: String = "/" + (tuple map { case (key, value) => s"$key=$value"} mkString "/")
}
