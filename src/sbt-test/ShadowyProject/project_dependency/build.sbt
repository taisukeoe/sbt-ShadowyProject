lazy val mySetting = settingKey[String]("my setting")
lazy val checkMySetting = taskKey[Unit]("my setting check")
lazy val checkScalacOptions = taskKey[Unit]("scalacOptions check")

lazy val mySettingValue = "shadowee"
lazy val fatalWarnings = "-Xfatal-warnings"
lazy val unused = "-Ywarn-unused"
lazy val deprecation = "-deprecation"

import SettingTransformer._

ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.2"


lazy val a1 = project
lazy val a2 = project.dependsOn(a1 % "compile->compile;test->test")
lazy val a3 = project.dependsOn(a2 % "compile->compile;test->test")

lazy val shade = project
  .shade(a3)
  .light
  .settings(
    scalacOptions ++= Seq("-Werror", "-Wunused")
  )
