package com.taisukeoe

import java.io.File

import sbt.Keys._
import sbt._

/*
 * Project sources, resources and local jars should be consistent between a shadowee and shadowers.
 */
object ProjectConsistency {
  val Configurations: Seq[Configuration] = Seq(Compile, Test)
  val Configs: Seq[ConfigKey] = Configurations.map(c => ConfigKey(c.name))

  val SettingKeysForManagedDir: Seq[SettingKey[File]] =
    Seq(
      sourceManaged,
      resourceManaged
    )

  val SettingKeysForUnmanagedDir: Seq[SettingKey[File]] =
    Seq(
      sourceDirectory,
      resourceDirectory,
      unmanagedBase,
      scalaSource,
      javaSource
    )

  val SettingKeysForUnmanagedDirs: Seq[SettingKey[Seq[File]]] =
    Seq(
      unmanagedSourceDirectories,
      unmanagedResourceDirectories
    )

  val SettingKeysForDirs: Seq[SettingKey[Seq[File]]] =
    Seq(
      sourceDirectories,
      resourceDirectories
    )

  val SettingKeysForManagedDirs: Seq[SettingKey[Seq[File]]] =
    Seq(
      managedSourceDirectories,
      managedResourceDirectories
    )

  val SettingKeysForGenerators: Seq[SettingKey[Seq[Task[Seq[File]]]]] = Seq(
    sourceGenerators,
    resourceGenerators
  )

  val TaskKeysForUnmanagedFiles: Seq[TaskKey[Seq[File]]] =
    Seq(
      unmanagedSources,
      unmanagedResources
    )

  val TaskKeysForManagedFiles: Seq[TaskKey[Seq[File]]] =
    Seq(
      managedSources,
      managedResources
    )

  val TaskKeysForAggregatedFiles: Seq[TaskKey[Seq[File]]] =
    Seq(
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

  val AllSettingKeys: Seq[SettingKey[_]] =
    SettingKeysForUnmanagedDir ++ SettingKeysForUnmanagedDirs ++ SettingKeysForManagedDir ++ SettingKeysForManagedDirs ++ SettingKeysForDirs ++ SettingKeysForGenerators

  val AllTaskKeys: Seq[TaskKey[_]] =
    TaskKeysForUnmanagedFiles ++ TaskKeysForClasspath ++ TaskKeysForManagedFiles ++ TaskKeysForAggregatedFiles
}
