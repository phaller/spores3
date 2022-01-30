val Scala30 = "3.0.2"
val Scala31 = "3.1.0"

ThisBuild / crossScalaVersions := Seq(Scala31, Scala30)
ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.last

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

lazy val sample = project
  .in(file("sample"))
  .dependsOn(blocks.jvm)
  .settings(
    name := "blocks-sample",
    version := "0.1.0-SNAPSHOT",
  )

lazy val blocksUpickle = crossProject(JVMPlatform, JSPlatform)
  .in(file("blocks-upickle"))
  .dependsOn(blocks)
  .settings(
    name := "blocks-upickle",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % "1.4.2",
  )
  .jvmSettings(
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .jsSettings(
    scalaJSUseMainModuleInitializer := true
  )
