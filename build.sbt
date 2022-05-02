lazy val Scala30 = "3.0.2"
lazy val Scala31 = "3.1.1"
lazy val supportedScalaVersions = List(Scala31, Scala30)
lazy val upickleVersion = "1.5.0"
lazy val junitInterfaceVersion = "0.11"

ThisBuild / organization := "com.phaller"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := supportedScalaVersions.head

ThisBuild / credentials += Credentials(
  "GnuPG Key ID",
  "gpg",
  "D83E7D99A038AF99DEDC841C73187B9E148329E6",
  "ignored"
)

lazy val root = project
  .in(file("."))
  .aggregate(blocks.jvm, blocks.js)
  .settings(
    // following instructions on cross building at:
    // https://www.scala-sbt.org/1.x/docs/Cross-Build.html
    crossScalaVersions := Nil,
    publish / skip := true,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
  )

lazy val blocks = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .settings(
    name := "blocks",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies += "com.lihaoyi" %%% "upickle" % upickleVersion,
  )
  .jvmSettings(
    libraryDependencies += "com.novocode" % "junit-interface" % junitInterfaceVersion % "test"
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))

lazy val sample = project
  .in(file("sample"))
  .dependsOn(blocks.jvm, blocksUpickle.jvm)
  .settings(
    name := "blocks-sample",
    crossScalaVersions := supportedScalaVersions,
    publish / skip := true,
  )

lazy val blocksUpickle = crossProject(JVMPlatform, JSPlatform)
  .in(file("blocks-upickle"))
  .dependsOn(blocks)
  .settings(
    name := "blocks-upickle",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies += "com.lihaoyi" %%% "upickle" % upickleVersion,
  )
  .jvmSettings(
    libraryDependencies += "com.novocode" % "junit-interface" % junitInterfaceVersion % "test"
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .jsSettings(
    scalaJSUseMainModuleInitializer := true
  )
