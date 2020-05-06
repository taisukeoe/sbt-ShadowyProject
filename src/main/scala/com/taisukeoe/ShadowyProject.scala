package com.taisukeoe

import sbt._

trait ShadowyProject[Shadowy] {
  def originalOf(shadowy: Shadowy): Project
  def settingsFor(shadowy: Shadowy, set: Seq[Setting[_]]): Shadowy
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

    settingsFor(shadower, newOverrides)
  }

  final def reflectSettingKeys[T](
      shadowy: Shadowy,
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[SettingKey[T]]
  ): Shadowy =
    reflectScopedKeys(shadowy, configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
      targetKey.in(originalScope) := targetKey.in(shadowScope).value
    )

  final def reflectTaskKeys[T](
      shadowy: Shadowy,
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[TaskKey[T]]
  ): Shadowy =
    reflectScopedKeys(shadowy, configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
      targetKey.in(originalScope) := targetKey.in(shadowScope).value
    )

  final def reflectInputKeys[T](
      shadowy: Shadowy,
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[InputKey[T]]
  ): Shadowy =
    reflectScopedKeys(shadowy, configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
      targetKey.in(originalScope) := targetKey.in(shadowScope).evaluated
    )
}
