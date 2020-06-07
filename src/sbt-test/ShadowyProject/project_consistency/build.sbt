lazy val shadowee = (project in file("shadowee"))
  .settings(
    version := "0.1",
    scalaVersion := "2.12.11",
    inConfig(Compile)(Seq(
      /*
       * Following sbt Keys are NOT used by sources key nor resources key, so that they are eventually ignored.
       *  managedSourceDirectories += baseDirectory.value / "managedSourceDirectories",
       *  sourceDirectories += baseDirectory.value / "sourceDirectories",
       *  managedResourceDirectories += baseDirectory.value / "managedResourceDirectories",
       *  resourceDirectories += baseDirectory.value / "resourceDirectories",
       *
       * sources or resources TaskKey cannot be used directly here due shadowy managedSources or managedResources dependency graph wiring.
       */
      javaSource := baseDirectory.value / "javaSource",
      resourceManaged := baseDirectory.value / "resourceManaged",
      managedResources += resourceManaged.value / "managedResources.conf",
      unmanagedResources += baseDirectory.value / "resourceDir" / "unmanagedResources.conf",
      resourceGenerators += Def.task((resourceManaged.value / "resourceGenerators").listFiles().toSeq).taskValue,
      scalaSource := baseDirectory.value / "scalaSource",
      sourceManaged := baseDirectory.value / "sourceManaged",
      managedSources += sourceManaged.value / "managedSources.scala",
      unmanagedSources += baseDirectory.value / "sourceDir" / "unmanagedSources.scala",
      sourceGenerators += Def.task((sourceManaged.value / "sourceGenerators").listFiles().toSeq).taskValue,
      unmanagedResourceDirectories += baseDirectory.value / "unmanagedResourceDirectories",
      unmanagedSourceDirectories += baseDirectory.value / "unmanagedSourceDirectories"
    )),
    inConfig(Compile)(
      /*
       * Following sbt Keys are NOT used by compile or run task.
       *  internalDependencyAsJars
       *  dependencyClasspathAsJars
       *
       * Following sbt Keys require compile.
       *  fullClasspath
       *  fullClasspathAsJars
       */
      Seq(unmanagedJars,
        unmanagedClasspath,
        managedClasspath,
        internalDependencyClasspath,
        externalDependencyClasspath,
        dependencyClasspath).map(
          key =>
            key += Attributed.blank(baseDirectory.value / "jars" / s"${key.key.label.capitalize}.jar")
      ))
  )

lazy val shadow = shadowee.autoShadow.light

lazy val shade = shadowee.autoShade.light
