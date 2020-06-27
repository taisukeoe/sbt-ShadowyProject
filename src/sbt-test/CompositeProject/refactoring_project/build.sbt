lazy val fatalWarnings = "-Xfatal-warnings"
lazy val unused = "-Ywarn-unused"
lazy val deprecation = "-deprecation"

lazy val shadowee = crossProject(JVMPlatform, Refactoring)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    version := "0.1",
    scalaVersion := "2.13.2",
    scalacOptions ++= Seq(unused, deprecation),
    unmanagedSourceDirectories.in(Compile) += baseDirectory.value / "raw",
  ).jvmSettings(
    scalacOptions.in(Compile, compile) += fatalWarnings
  )
