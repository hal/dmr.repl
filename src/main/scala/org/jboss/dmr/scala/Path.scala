package org.jboss.dmr.scala

/** Helper class to read (nested) keys from a model node using a path like syntax */
case class Path(elements: List[String]) {
  def /(path: Path) = Path(elements ++ path.elements)
}
