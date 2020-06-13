package com.taisukeoe

import java.nio.file.Files

import scala.collection.JavaConverters._

import com.taisukeoe.{ProjectConsistency => PC}

import sbt._

trait ShadowyProject[Shadowy] {
  def originalOf(shadowy: Shadowy): Project
  def settingsFor(set: Seq[Setting[_]])(shadowy: Shadowy): Shadowy
  def light(shadowy: Shadowy): Project

  private def reflectScopedKeys[KeyType](
      shadower: Shadowy,
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[KeyType]
  )(settingExp: (KeyType, Scope, Scope) => Setting[_]): Shadowy = {
    val newOverrides = for {
      c <- if (configs.nonEmpty) configs.map(Select(_)) else Seq(This)
      t <- if (tasks.nonEmpty) tasks.map(Select(_)) else Seq(This)
      targetKey <- keys
    } yield settingExp(
      targetKey,
      Scope(This, c, t, This),
      Scope(Select(originalOf(shadower)), c, t, This)
    )

    settingsFor(newOverrides)(shadower)
  }

  final def reflectSettingKeys[T](
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[SettingKey[T]]
  )(shadowy: Shadowy): Shadowy =
    reflectScopedKeys(shadowy, configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
      targetKey.in(originalScope) := targetKey.in(shadowScope).value
    )

  final def reflectTaskKeys[T](
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[TaskKey[T]]
  )(shadowy: Shadowy): Shadowy =
    reflectScopedKeys(shadowy, configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
      targetKey.in(originalScope) := targetKey.in(shadowScope).value
    )

  final def reflectInputKeys[T](
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[InputKey[T]]
  )(shadowy: Shadowy): Shadowy =
    reflectScopedKeys(shadowy, configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
      targetKey.in(originalScope) := targetKey.in(shadowScope).evaluated
    )

  final def isConsistentAt(configs: Seq[ConfigKey], tasks: Seq[AttributeKey[_]])(shadowy: Shadowy): Shadowy = {
    /*
     * Since resourceGenerators in sbt plugin project lead to compile,
     * resourceGenerators and managedResources in original project scope must be independent from shadowy projects.
     *
     * Instead, all files under sourceManaged or resourceManaged directories are captured to shadowy managedSources or managedResources respectively.
     */
    val configTaskMatrix: Seq[(ScopeAxis[ConfigKey], ScopeAxis[AttributeKey[_]])] =
      for {
        cfg <- if (configs.nonEmpty) configs.map(Select(_)) else Seq(This)
        tsk <- if (tasks.nonEmpty) tasks.map(Select(_)) else Seq(Zero)
      } yield (cfg, tsk)

    val managedSettings: Seq[Seq[Setting[_]]] = for {
      (cfg, tsk) <- configTaskMatrix
      (
        managedSourcesOrResources,
        managedSourceOrResourceDirectories,
        sourceOrResourceManaged
      ) <- (
          PC.TaskKeysForManagedFiles,
          PC.SettingKeysForManagedDirs,
          PC.SettingKeysForManagedDir
      ).zipped
    } yield Seq(
      managedSourceOrResourceDirectories.in(Scope(This, cfg, tsk, Zero)) += sourceOrResourceManaged
        .in(Scope(Select(originalOf(shadowy)), cfg, tsk, Zero))
        .value,
      managedSourcesOrResources.in(Scope(This, cfg, tsk, Zero)) ++= {
        val managedDir =
          sourceOrResourceManaged
            .in(Scope(Select(originalOf(shadowy)), cfg, tsk, Zero))
            .value
        if (managedDir.exists)
          for {
            path <- Files.walk(managedDir.toPath).iterator().asScala.toSeq
            file = path.toFile
            if file.isFile
          } yield file
        else
          Nil
      }
    )

    // sbt-plugin projects have resource generators, which may cause resource files duplication in ShadowyProject.
    val emptyGenerators: Seq[Setting[_]] = for {
      (cfg, tsk) <- configTaskMatrix
      sourceOrResourceGenerators <- PC.SettingKeysForGenerators
    } yield sourceOrResourceGenerators.in(Scope(This, cfg, tsk, Zero)) := Nil

    reflectSettingKeys(configs, tasks, PC.SettingKeysForUnmanagedDir) _ andThen
      reflectSettingKeys(
        configs,
        tasks,
        PC.SettingKeysForUnmanagedDirs
      ) andThen
      reflectTaskKeys(configs, tasks, PC.TaskKeysForClasspath) andThen
      reflectTaskKeys(
        configs,
        tasks,
        PC.TaskKeysForUnmanagedFiles
      ) andThen
      settingsFor(managedSettings.flatten) andThen
      settingsFor(emptyGenerators) apply shadowy
  }
}

object ShadowyProject {
  def apply[T](implicit S: ShadowyProject[T]): ShadowyProject[T] = S

  implicit class toOps[Shadowy: ShadowyProject](override val shadowy: Shadowy) extends Ops[Shadowy]

  trait Ops[Shadowy] {
    def shadowy: Shadowy

    def isConsistentAt(configs: ConfigKey*)(implicit EV: ShadowyProject[Shadowy]): Shadowy =
      isConsistentAt(configs, Nil)

    def isConsistentAt(configs: Seq[ConfigKey], tasks: Seq[AttributeKey[_]])(implicit
        EV: ShadowyProject[Shadowy]
    ): Shadowy = EV.isConsistentAt(configs, tasks)(shadowy)

    def reflectSettingKeys[T](
        configs: Seq[ConfigKey],
        tasks: Seq[AttributeKey[_]],
        keys: Seq[SettingKey[T]]
    )(implicit EV: ShadowyProject[Shadowy]): Shadowy = EV.reflectSettingKeys(configs, tasks, keys)(shadowy)

    def reflectTaskKeys[T](
        configs: Seq[ConfigKey],
        tasks: Seq[AttributeKey[_]],
        keys: Seq[TaskKey[T]]
    )(implicit EV: ShadowyProject[Shadowy]): Shadowy = EV.reflectTaskKeys(configs, tasks, keys)(shadowy)

    def reflectInputKeys[T](
        configs: Seq[ConfigKey],
        tasks: Seq[AttributeKey[_]],
        keys: Seq[InputKey[T]]
    )(implicit EV: ShadowyProject[Shadowy]): Shadowy = EV.reflectInputKeys(configs, tasks, keys)(shadowy)

    def settings(set: Setting[_]*)(implicit EV: ShadowyProject[Shadowy]): Shadowy = EV.settingsFor(set)(shadowy)

    def light(implicit EV: ShadowyProject[Shadowy]): Project = EV.light(shadowy)
  }
}
