lazy val fatalWarnings = "-Xfatal-warnings"
lazy val unused = "-Ywarn-unused"
lazy val deprecation = "-deprecation"

lazy val prj = refactoringProject()
  .settings(
    version := "0.1",
    scalaVersion := "2.13.2",
    scalacOptions ++= Seq(unused, deprecation),
    unmanagedSourceDirectories.in(Compile) += baseDirectory.value / "raw",
    scalacOptions += fatalWarnings
  )
//  .primarySettings(
//    scalacOptions.in(Compile, compile) += fatalWarnings
//  )

import SettingTransformer._

lazy val shadow = prj.refactor.modify(RemoveXFatalWarnings)
