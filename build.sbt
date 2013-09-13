name := """dmr-repl"""

version := "1.0"

scalaVersion := "2.10.2"

retrieveManaged := true

resolvers ++= Seq(
    "spray repo" at "http://nightlies.spray.io",
    "spray repo2" at "http://repo.spray.io"
    )

libraryDependencies ++= {
    val sprayVersion = "1.2-20130710"
    Seq(
        "io.spray" %  "spray-client" % sprayVersion,
        "io.spray" %  "spray-httpx" % "1.2-20130822",
        "io.spray" %%  "spray-json" % "1.2.5",
        "org.scalatest" %% "scalatest" % "1.9.1" % "test",
        "junit" % "junit" % "4.11" % "test",
        "com.novocode" % "junit-interface" % "0.7" % "test->default"
    )
}

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

