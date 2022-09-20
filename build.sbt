lazy val Scala30 = "3.0.2"
lazy val Scala31 = "3.1.2"
lazy val supportedScalaVersions = List(Scala31, Scala30)
lazy val upickleVersion = "2.0.0"
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
  .aggregate(spores.jvm, spores.js)
  .settings(
    // following instructions on cross building at:
    // https://www.scala-sbt.org/1.x/docs/Cross-Build.html
    crossScalaVersions := Nil,
    publish / skip := true,
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
  )

lazy val spores = crossProject(JVMPlatform, JSPlatform)
  .in(file("."))
  .settings(
    name := "spores3",
    crossScalaVersions := supportedScalaVersions,
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
    crossScalaVersions := supportedScalaVersions,
    publish / skip := true,
  )
