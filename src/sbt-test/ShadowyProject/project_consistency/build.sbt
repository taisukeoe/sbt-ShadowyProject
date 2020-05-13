lazy val shadowee = (project in file("shadowee"))
  .settings(
    version := "0.1",
    scalaVersion := "2.12.11",
    inConfig(Compile)(Seq(
      /*
       * Following sbt Keys are NOT used by sbt.Keys.sources, so that they are eventually ignored.
       *  managedSourceDirectories += baseDirectory.value / "managedSourceDirectories",
       *  sourceDirectories += baseDirectory.value / "sourceDirectories",
       *  sourceManaged := baseDirectory.value / "sourceManaged",
       */
      managedSources += baseDirectory.value / "managedSources" / "s.scala",
      scalaSource := baseDirectory.value / "scalaSource",
      sourceGenerators += Def.task((baseDirectory.value / "sourceGenerators").listFiles().toSeq).taskValue,
      sources += baseDirectory.value / "Sources" / "s.scala",
      unmanagedSourceDirectories += baseDirectory.value / "unmanagedSourceDirectories",
      unmanagedSources += baseDirectory.value / "unmanagedSources" / "s.scala",
    ))
  )

lazy val shadow = shadowee.autoShadow.light

lazy val shade = shadowee.autoShade.light
