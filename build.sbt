ThisBuild / git.baseVersion := "0.1"
ThisBuild / organization := "com.taisukeoe"
ThisBuild / description := "Define multiple sbt sub-projects which share sources, resources and jars"
ThisBuild / licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

import SettingTransformer._

lazy val sbtShadowyProject = (project in file("."))
  .enablePlugins(SbtPlugin)
  .enablePlugins(GitVersioning)
  .disablePlugins(ScalafixPlugin)
  .settings(
    name := "sbt-shadowyproject",
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
    publishMavenStyle := false,
    bintrayRepository := "sbt-ShadowyProject",
    bintrayOrganization in bintray := None,
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
  .settings(ScalafixSettings.permanent: _*)
  .light
