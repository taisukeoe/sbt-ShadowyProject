lazy val shadowee = (project in file("shadowee"))
  .settings(
    version := "0.1",
    scalaVersion := "2.12.11",
    Compile / unmanagedSourceDirectories += baseDirectory.value / "raw",
  )

lazy val shadowOfShadowee = project
  .shadow(shadowee)
  .light

lazy val shadeOfShadowee = project
  .shade(shadowee)
  .light
