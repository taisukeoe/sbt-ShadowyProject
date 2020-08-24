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

val myRun = TaskKey[Unit]("myRun")

val context = new ShadowyContext(Empty,
  ForAll.settings(myRun := (Compile / run).toTask(" a1 a2 a3 a4").value),
  ForSecondary.settings(scalacOptions += "-Wunused"),
  ForAll.autoAggregate(myRun)
)

lazy val a1 = shadowyProject(context).settings(libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.10")
lazy val a2 = shadowyProject(context).dependsOn(a1 % "compile->compile;test->test")
lazy val a3 = shadowyProject(context).dependsOn(a2 % "compile->compile;test->test")
lazy val a4 = shadowyProject(context).dependsOn(a3)
