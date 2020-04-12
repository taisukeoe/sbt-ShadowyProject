import SettingTransformer._

lazy val sbtShadowProjects = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-shadowprojects",
    organization := "dev.taisukeoe",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.11",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Xlint"
    ),
    Compile / compile / scalacOptions += "-Xfatal-warnings",
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
  .modify(RemoveScalacOptions("-Xfatal-warnings", "-Xlint"))
  .shadow(sbtShadowProjects)
  .settings(ScalafixSettings.permanent)
