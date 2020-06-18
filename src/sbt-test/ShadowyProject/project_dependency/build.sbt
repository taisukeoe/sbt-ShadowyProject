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
lazy val a3 = project.dependsOn(a2)
lazy val a4 = project.dependsOn(a3 % "compile->compile;test->test")
lazy val a5 = project.dependsOn(a4 % "compile->compile;test->test")

val common = scalacOptions += "-Wunused"

lazy val deepShadowA2 = project
  .deepShadow(a2)
  .settings(common)
  .light

lazy val deepShadowA3 = project
  .deepShadow(a3)
  .settings(common)
  .light

lazy val deepShadowA4 = project
  .deepShadow(a4)
  .settings(common)
  .light

lazy val deepShadowA5 = project
  .deepShadow(a5)
  .settings(common)
  .light

lazy val shadow = project
  .shadow(a5)
  .settings(common)
  .light
