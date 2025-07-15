lazy val Scala33 = "3.3.6"
lazy val upickleVersion = "3.1.0"
lazy val junitInterfaceVersion = "0.11"

ThisBuild / organization := "com.phaller"
ThisBuild / organizationName := "Philipp Haller"
ThisBuild / organizationHomepage := Some(url("https://www.phaller.com/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/phaller/spores3"),
    "scm:git@github.com:phaller/spores3.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "phaller",
    name  = "Philipp Haller",
    email = "hallerp@gmail.com",
    url   = url("https://github.com/phaller")
  ),
  Developer(
    id    = "jspenger",
    name  = "Jonas Spenger",
    email = "jonas.spenger@gmail.com",
    url   = url("https://github.com/jspenger")
  )
)

ThisBuild / description := "Spores3 provides abstractions for making closures in Scala safer and more flexible"
ThisBuild / licenses := List("Apache-2.0" -> new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/phaller/spores3"))

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

ThisBuild / version      := "0.2.0-SNAPSHOT"
ThisBuild / scalaVersion := Scala33

ThisBuild / credentials += Credentials(
  "GnuPG Key ID",
  "gpg",
  "B4CC0C56EBBBC95D23D14C454ADDDD4698B3BC95",
  "ignored"
)


lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("core"))
  .settings(
    name := "spores3",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % upickleVersion,
    libraryDependencies += "com.novocode" % "junit-interface" % junitInterfaceVersion % "test",
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .nativeConfigure(_.enablePlugins(ScalaNativeJUnitPlugin))


lazy val sample = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("sample"))
  .settings(
    name := "spores3-sample",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % upickleVersion,
    publish / skip := true,
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := true,
  )
  .dependsOn(core)
