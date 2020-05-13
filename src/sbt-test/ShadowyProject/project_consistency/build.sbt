lazy val shadowee = (project in file("shadowee"))
  .settings(
    version := "0.1",
    scalaVersion := "2.12.11",
    inConfig(Compile)(Seq(
      /*
       * Following sbt Keys are NOT used by sources key nor resources key, so that they are eventually ignored.
       *  managedSourceDirectories += baseDirectory.value / "managedSourceDirectories",
       *  sourceDirectories += baseDirectory.value / "sourceDirectories",
       *  sourceManaged := baseDirectory.value / "sourceManaged",
       *  managedResourceDirectories += baseDirectory.value / "managedResourceDirectories",
       *  resourceDirectories += baseDirectory.value / "resourceDirectories",
       *  resourceManaged := baseDirectory.value / "resourceManaged",
       */
      javaSource := baseDirectory.value / "javaSource",
      managedResources += baseDirectory.value / "resourceDir" / "managedResources.conf",
      resources += baseDirectory.value / "resourceDir" / "resources.conf",
      unmanagedResources += baseDirectory.value / "resourceDir" / "unmanagedResources.conf",
      resourceGenerators += Def.task((baseDirectory.value / "resourceGenerators").listFiles().toSeq).taskValue,
      scalaSource := baseDirectory.value / "scalaSource",
      managedSources += baseDirectory.value / "sourceDir" / "managedSources.scala",
      sources += baseDirectory.value / "sourceDir" / "sources.scala",
      unmanagedSources += baseDirectory.value / "sourceDir" / "unmanagedSources.scala",
      sourceGenerators += Def.task((baseDirectory.value / "sourceGenerators").listFiles().toSeq).taskValue,
      unmanagedResourceDirectories += baseDirectory.value / "unmanagedResourceDirectories",
      unmanagedSourceDirectories += baseDirectory.value / "unmanagedSourceDirectories"
    ))
  )

lazy val shadow = shadowee.autoShadow.light

lazy val shade = shadowee.autoShade.light
