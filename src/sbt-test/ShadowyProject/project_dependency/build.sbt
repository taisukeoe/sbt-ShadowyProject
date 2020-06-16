lazy val mySetting = settingKey[String]("my setting")
lazy val checkMySetting = taskKey[Unit]("my setting check")
lazy val checkScalacOptions = taskKey[Unit]("scalacOptions check")

lazy val mySettingValue = "shadowee"
lazy val fatalWarnings = "-Xfatal-warnings"
lazy val unused = "-Ywarn-unused"
lazy val deprecation = "-deprecation"

import SettingTransformer._

version.in(ThisBuild) := "0.1"
scalaVersion.in(ThisBuild) := "2.13.2"

lazy val a1 = project.settings(libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.10")
lazy val a2 = project.dependsOn(a1 % "compile->compile;test->test")
lazy val a3 = project.dependsOn(a2 % "compile->compile;test->test")

lazy val deepShadow = project
  .deepShadow(a3)
  .light

lazy val shadow = project
  .shadow(a3)
  .light
