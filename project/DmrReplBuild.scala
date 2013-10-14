import sbt._

object DmrReplBuild extends Build {
  lazy val root = Project("root", file(".")) dependsOn (dmrScala)
  lazy val dmrScala = RootProject(uri("git://github.com/hpehl/dmr.scala.git"))
}
