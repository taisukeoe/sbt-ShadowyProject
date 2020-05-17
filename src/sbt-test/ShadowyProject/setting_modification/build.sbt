lazy val mySetting = settingKey[String]("my setting")
lazy val checkMySetting = taskKey[Unit]("my setting check")
lazy val checkScalacOptions = taskKey[Unit]("scalacOptions check")

lazy val mySettingValue = "shadowee"
lazy val fatalWarnings = "-Xfatal-warnings"
lazy val unused = "-Ywarn-unused"
lazy val deprecation = "-deprecation"

import SettingTransformer._

lazy val shadowee = (project in file("shadowee"))
  .settings(
    version := "0.1",
    scalaVersion := "2.13.2",
    mySetting.in(Runtime) := "shadowee runtime",
    scalacOptions ++= Seq(unused, deprecation),
    scalacOptions.in(Compile, compile) += fatalWarnings,
    unmanagedSourceDirectories.in(Compile) += baseDirectory.value / "raw",
    mySetting := mySettingValue
  )

lazy val shadowOfShadowee = project
  .shadow(shadowee)
  .modify(ExcludeConfigScoped(Set(Runtime)) + RemoveXFatalWarnings + RemoveScalacOptions(unused))
  .settings(
    checkMySetting := {
      val foundValue = mySetting.in(Runtime).value
      assert(foundValue == mySettingValue, s"Runtime / mySetting value is $foundValue. Expected is $mySettingValue")
    },
    checkScalacOptions := {
      val foundValue = scalacOptions.in(Compile, compile).value
      assert(foundValue.contains(deprecation), s"$deprecation should keep.")
      assert(!foundValue.contains(fatalWarnings), s"$fatalWarnings should be removed.")
      assert(!foundValue.contains(unused), s"$unused should be removed.")
    }
  ).light

lazy val shadeOfShadowee = project
  .shade(shadowee)
  .settings(
    checkScalacOptions := {
      val foundValue = scalacOptions.in(Compile, compile).value
      assert(!foundValue.contains(deprecation), s"$deprecation should not be included.")
    }
  )
  .light
