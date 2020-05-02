import SettingTransformer._

lazy val sbtShadowyProject = (project in file("."))
  .enablePlugins(SbtPlugin)
  .disablePlugins(ScalafixPlugin)
  .settings(
    name := "sbt-shadowyproject",
    organization := "com.taisukeoe",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.11",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Xlint"
    ),
    Compile / compile / scalacOptions += "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.1.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
    ),
    sbtPlugin := true,
    pluginCrossBuild / sbtVersion := "1.2.8",
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
  .settings(Seq(Compile, Test).map(_ / console / scalacOptions -= "-Xlint"))

lazy val shadow = project
  .shadow(sbtShadowyProject) //dog-fooding!
  /*
    Since -Xfatal-warnings prevent RemoveUnused scalafix rule from working,
    this shadow project is nicer to run scalafix.
   */
  .modify(RemoveScalacOptions("-Xfatal-warnings", "-Xlint"))
  .light
  .settings(ScalafixSettings.permanent)
