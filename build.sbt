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

lazy val blocksUpickle = crossProject(JVMPlatform, JSPlatform)
  .in(file("blocks-upickle"))
  .settings(
    name := "blocks-upickle",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "1.3.9",
  )
  .jvmSettings(
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .jsSettings(
    scalaJSUseMainModuleInitializer := true
  )
