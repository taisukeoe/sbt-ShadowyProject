lazy val mySetting = settingKey[String]("my setting")
lazy val checkMySetting = taskKey[Unit]("my setting check")
lazy val checkScalacOptions = taskKey[Unit]("scalacOptions check")

lazy val mySettingValue = "shadowee"
lazy val fatalWarnings = "-Xfatal-warnings"

import SettingTransformer._

lazy val shadowee = (project in file("shadowee"))
  .settings(
    version := "0.1",
    scalaVersion := "2.13.1",
    Runtime / mySetting := "shadowee runtime",
    Compile / compile / scalacOptions += fatalWarnings,
    mySetting := mySettingValue
  )

lazy val shadower = project
  .shadow(shadowee)
  .modify(ExcludeConfigScoped(Set(Runtime)) || RemoveXFatalWarnings)
  .light
  .settings(
    checkMySetting := {
      val foundValue = (Runtime / mySetting).value
      assert(foundValue == mySettingValue, s"Runtime / mySetting value is $foundValue. Expected is $mySettingValue")
    },
    checkScalacOptions := {
      val foundValue = (Compile / compile / scalacOptions).value
      assert(!foundValue.contains(fatalWarnings), s"-Xfatal-warnings should be removed.")
    },
  )

