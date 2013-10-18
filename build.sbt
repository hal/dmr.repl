name := "dmr-repl"

organization := "org.jboss"

version := "0.1.0"

scalaVersion := "2.10.2"

retrieveManaged := true

scalacOptions ++= Seq(
  "-language:implicitConversions",
  "-language:postfixOps",
  "-feature",
  "-deprecation"
)

resolvers ++= Seq(
  "JBoss Repository" at "https://repository.jboss.org/nexus/content/groups/public/"
)

// Dependency to DMR.scala is defined in DmrReplBuild.scala as GitHub dependency
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "org.wildfly" % "wildfly-controller-client" % "8.0.0.Alpha4"
)

initialCommands += """
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.language.implicitConversions
  import org.jboss.dmr.scala._
  import org.jboss.dmr.repl._
  import org.jboss.dmr.repl.Client._
  import org.jboss.dmr.repl.Storage._
                   """
