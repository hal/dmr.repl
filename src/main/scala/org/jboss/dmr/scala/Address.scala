package org.jboss.dmr.scala

/** Helper class for holding the address tuple for a model node */
case class Address(tuple: List[(String, String)]) {
  def /(address: Address) = Address(tuple ++ address.tuple)
}
