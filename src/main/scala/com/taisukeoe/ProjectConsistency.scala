package com.taisukeoe

import java.io.File

import sbt.Keys._
import sbt._

/*
 * Project sources, resources and local jars should be consistent between a shadowee and shadowers.
 */
object ProjectConsistency {
  val Configs: Seq[ConfigKey] = Seq(Compile, Test)

  val SettingKeysForDir: Seq[SettingKey[File]] =
    Seq(
      sourceDirectory,
      resourceDirectory,
      unmanagedBase,
      scalaSource,
      javaSource,
      sourceManaged,
      resourceManaged
    )

  val SettingKeysForFiles: Seq[SettingKey[Seq[File]]] =
    Seq(
      unmanagedSourceDirectories,
      managedSourceDirectories,
      unmanagedResourceDirectories,
      managedResourceDirectories,
      sourceDirectories,
      resourceDirectories
    )

  val SettingKeysForGenerators: Seq[SettingKey[Seq[Task[Seq[File]]]]] = Seq(
    sourceGenerators,
    resourceGenerators
  )

  val TaskKeysForFiles: Seq[TaskKey[Seq[File]]] =
    Seq(
      unmanagedSources,
      unmanagedResources,
      managedSources,
      managedResources,
      sources,
      resources
    )

  val TaskKeysForClasspath: Seq[TaskKey[Classpath]] = Seq(
    unmanagedJars,
    unmanagedClasspath,
    managedClasspath,
    internalDependencyClasspath,
    externalDependencyClasspath,
    dependencyClasspath
  )

  val SettingKeys: Seq[SettingKey[_]] =
    SettingKeysForDir ++ SettingKeysForFiles ++ SettingKeysForGenerators

  val TaskKeys: Seq[TaskKey[_]] = TaskKeysForFiles ++ TaskKeysForClasspath
}
