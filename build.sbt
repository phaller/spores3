lazy val Scala33 = "3.3.1"
lazy val upickleVersion = "3.0.0"
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

lazy val root = project
  .in(file("."))
  .aggregate(spores.jvm, spores.js)
  .settings(
    publish / skip := true,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
  )

lazy val spores = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .settings(
    name := "spores3",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % upickleVersion,
  )
  .jvmSettings(
    libraryDependencies += "com.novocode" % "junit-interface" % junitInterfaceVersion % "test"
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))

lazy val sample = project
  .in(file("sample"))
  .dependsOn(spores.jvm)
  .settings(
    name := "spores3-sample",
    publish / skip := true,
  )
