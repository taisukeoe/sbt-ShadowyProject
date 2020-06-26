def scala212 = "2.12.11"
def scala210 = "2.10.7"

ThisBuild / git.baseVersion := "0.1"
ThisBuild / organization := "com.taisukeoe"
ThisBuild / description := "Define multiple sbt sub-projects which share sources, resources and jars"
ThisBuild / licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
ThisBuild / scalaVersion := scala212

import SettingTransformer._

def ifScala212[T](binVersion: String)(ifTrue: T)(ifFalse: T): T =
  if (binVersion == "2.12") ifTrue else ifFalse

import MyPlatformOps._

lazy val main = crossProject(JVMPlatform, MyPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Full)
  .enablePlugins(SbtPlugin)
  .enablePlugins(GitVersioning)
  //  .disablePlugins(ScalafixPlugin)
  .settings(
    name := "sbt-shadowyproject",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
    scalacOptions ++= ifScala212(scalaBinaryVersion.value)(
      Seq(
        // Avoid unused warnings flag here, and let them manage by my shadow/scalafix.
        "-Xlint:adapted-args",
        "-Xlint:nullary-unit",
        "-Xlint:inaccessible",
        "-Xlint:nullary-override",
        "-Xlint:infer-any",
        "-Xlint:missing-interpolator",
        "-Xlint:doc-detached",
        "-Xlint:private-shadow",
        "-Xlint:type-parameter-shadow",
        "-Xlint:poly-implicit-overload",
        "-Xlint:option-implicit",
        "-Xlint:delayedinit-select",
        "-Xlint:package-object-classes",
        "-Xlint:stars-align",
        "-Xlint:constant"
      )
    )(
      Seq(
        "-Xlint",
        "-language:existentials"
      )
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.1.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
    ),
    sbtPlugin := true,
    crossScalaVersions := Seq(scala212, scala210),
    sbtVersion in pluginCrossBuild :=
      ifScala212(scalaBinaryVersion.value)("1.2.8")("0.13.17"),
    publishMavenStyle := false,
    bintrayRepository := "sbt-ShadowyProject",
    bintrayOrganization in bintray := None,
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
  .jvmSettings(
    Compile / compile / scalacOptions += "-Xfatal-warnings"
  )
  .mySettings(
    ScalafixSettings.permanent
  )

lazy val shadow = project
  .shadow(main.jvm) //dog-fooding!
  /*
   * Since Scalafix won't work well with `-Xfatal-warnings` or Scala 2.10,
   * this shadow project is nicer to run scalafix.
   */
  .modify(
    RemoveXFatalWarnings + ExcludeKeyNames(
      Set(crossScalaVersions.key.label)
    )
  )
  .settings(ScalafixSettings.permanent: _*)
  .light
