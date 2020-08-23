import SettingTransformer._

lazy val fatalWarnings = "-Xfatal-warnings"
lazy val unused = "-Ywarn-unused"
lazy val deprecation = "-deprecation"

lazy val context = new ShadowyContext(RemoveXFatalWarnings)

lazy val prj = shadowyProject(context)
  .settings(
    version := "0.1",
    scalaVersion := "2.13.2",
    scalacOptions ++= Seq(unused, deprecation),
    unmanagedSourceDirectories.in(Compile) += baseDirectory.value / "raw",
    scalacOptions += fatalWarnings
  )
