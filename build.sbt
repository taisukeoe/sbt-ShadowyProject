enablePlugins(SbtPlugin)

name := "sbt-shadowprojects"

organization := "dev.taisukeoe"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.11"

sbtPlugin := true

pluginCrossBuild / sbtVersion := "1.2.8"

scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}

scriptedBufferLog := false
