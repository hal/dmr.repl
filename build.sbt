name := "dmr-repl"

organization := "org.jboss"

version := "0.1.0"

scalaVersion := "2.10.3"

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

publishMavenStyle := true

pomExtra :=
  <licenses>
    <license>
      <name>lgpl</name>
      <url>http://repository.jboss.com/licenses/lgpl.txt</url>
    </license>
  </licenses>
    <url>
      https://github.com/hal/dmr.scala
    </url>

publishTo <<= version { (v: String) =>
  val nexus = "https://repository.jboss.org/nexus/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
