lazy val shadowee = (project in file("shadowee"))
  .settings(
    version := "0.1",
    scalaVersion := "2.12.11",
    Compile / unmanagedSourceDirectories += baseDirectory.value / "raw",
  )

lazy val shadow = shadowee.autoShadow.light

lazy val shade = shadowee.autoShade.light
