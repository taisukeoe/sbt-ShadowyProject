package com.taisukeoe

import java.io.File

import sbt.Keys._
import sbt._

/*
 * Project sources, resources and local jars should be consistent between a shadowee and shadowers.
 */
object ProjectConsistency {
  val DefaultSettingKeys: Seq[SettingKey[File]] =
    Seq(sourceDirectory, resourceDirectory, unmanagedBase)

  val SupplementalSettingKeys: Seq[SettingKey[_]] = Seq(
    scalaSource,
    unmanagedSourceDirectories,
    unmanagedResourceDirectories,
    javaSource
  )

  val SupplementalTaskKeys: Seq[TaskKey[_]] = Seq(
    unmanagedSources,
    unmanagedResources,
    unmanagedJars,
    unmanagedClasspath
  )
}
