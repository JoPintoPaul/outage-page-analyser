ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "outage-page-analyser",
    libraryDependencies += "org.jsoup" % "jsoup" % "1.11.3"
  )
