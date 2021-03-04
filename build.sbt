lazy val root = project
  .in(file("."))
  .settings(
    name := "blocks",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.0.0-RC1",
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
