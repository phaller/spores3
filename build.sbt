ThisBuild / scalaVersion := "3.0.0-RC1"

lazy val root = project
  .in(file("."))
  .aggregate(blocks.jvm, blocks.js)
  .settings(
    publish := {},
    publishLocal := {},
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
  )

lazy val blocks = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .settings(
    name := "blocks",
    version := "0.1.0-SNAPSHOT",
  )
  .jvmSettings(
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
