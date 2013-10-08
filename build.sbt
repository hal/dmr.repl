name := """dmr-repl"""

organization := "org.jboss.dmr"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

retrieveManaged := true

resolvers ++= Seq(
    "spray repo" at "http://nightlies.spray.io",
    "spray repo2" at "http://repo.spray.io",
    "JBoss Repository" at "https://repository.jboss.org/nexus/content/groups/public/"
    )

 initialCommands += """
    import scala.concurrent.ExecutionContext.Implicits.global
    import org.jboss.dmr.scala._
    import org.jboss.dmr.scala.ModelNode
    import org.jboss.dmr.scala.Response
    import org.jboss.dmr.scala.ShellCommands._
 """

libraryDependencies ++= {
    val sprayVersion = "1.2-20130710"
    Seq(
        "org.scalatest" %% "scalatest" % "1.9.1" % "test",
        "org.jboss.dmr" %% "dmr-scala" % "0.1-SNAPSHOT",
        "junit" % "junit" % "4.11" % "test",
        "com.novocode" % "junit-interface" % "0.7" % "test->default",
        "org.wildfly" % "wildfly-controller-client" % "8.0.0.Alpha4"
    )
}

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
