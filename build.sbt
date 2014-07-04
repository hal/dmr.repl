name := "dmr-repl"

organization := "org.jboss"

version := "0.2.2"

scalaVersion := "2.11.1"

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
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.wildfly" % "wildfly-controller-client" % "8.1.0.Final"
)

initialCommands += """
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.language.implicitConversions
  import org.jboss.dmr.scala._
  import org.jboss.dmr.scala.ModelNode
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
  <url>https://github.com/hal/dmr.repl</url>
  <scm>
    <connection>scm:git@github.com:hal/dmr.repl.git</connection>
    <developerConnection>scm:git:git@github.com:hal/dmr.repl.git</developerConnection>
    <url>git@github.com:hal/dmr.repl.git</url>
  </scm>
  <developers>
    <developer>
      <name>Heiko Braun</name>
      <id>hbraun</id>
      <email>hbraun@redhat.com</email>
      <organization>Red Hat</organization>
      <timezone>+1</timezone>
    </developer>
    <developer>
      <name>Harald Pehl</name>
      <id>hpehl</id>
      <email>hpehl@redhat.com</email>
      <organization>Red Hat</organization>
      <timezone>+1</timezone>
    </developer>
  </developers>

publishTo <<= version { (v: String) =>
  val nexus = "https://repository.jboss.org/nexus/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
