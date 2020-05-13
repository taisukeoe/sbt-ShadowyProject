lazy val shadowee = (project in file("shadowee"))
  .settings(
    version := "0.1",
    scalaVersion := "2.12.11",
    inConfig(Compile)(Seq(
      managedSourceDirectories += baseDirectory.value / "managedSourceDirectories",
      managedSources += baseDirectory.value / "managedSources" / "s.scala",
      scalaSource := baseDirectory.value / "scalaSource",
      sourceDirectories += baseDirectory.value / "sourceDirectories",
      sourceGenerators += Def.task((baseDirectory.value / "sourceGenerators").listFiles().toSeq).taskValue,
      sourceManaged := baseDirectory.value / "sourceManaged",
      sources += baseDirectory.value / "Sources" / "s.scala",
      unmanagedSourceDirectories += baseDirectory.value / "unmanagedSourceDirectories",
      unmanagedSources += baseDirectory.value / "unmanagedSources" / "s.scala",
    ))
  )

lazy val shadow = shadowee.autoShadow.light

lazy val shade = shadowee.autoShade.light
