package com.taisukeoe

import sbt._

object ShadowyProject {
  type Ops = ShadowyProject => ShadowyProject
  /*
   * Just a shorthand method to define an ops like:
   *
   * lazy val ops = ShadowyProject.ops(_.modify(RemoveXFatalWarnings))
   */
  def ops(f: Ops): Ops = f
}

//Use the constructor directly if you want to change above default arguments.
class ShadowyProject(
    thisProject: Project,
    shadowee: Project,
    trans: SettingTransformer,
    settingOverrides: Seq[Setting[_]]
) {
  private def shadowScopedKeys[KeyType](
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[KeyType]
  )(settingExp: (KeyType, Scope, Scope) => Setting[_]): ShadowyProject = {
    val newOverrides = for {
      c <- if (configs.nonEmpty) configs.map(Select(_)) else Seq(This)
      t <- if (tasks.nonEmpty) tasks.map(Select(_)) else Seq(This)
      targetKey <- keys
    } yield settingExp(targetKey, Scope(This, c, t, This), Scope(Select(shadowee), c, t, This))

    settings(newOverrides: _*)
  }

  def shadowSettingKeys[T](
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[SettingKey[T]]
  ): ShadowyProject =
    shadowScopedKeys(configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
      targetKey.in(originalScope) := targetKey.in(shadowScope).value
    )

  def shadowTaskKeys[T](
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[TaskKey[T]]
  ): ShadowyProject =
    shadowScopedKeys(configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
      targetKey.in(originalScope) := targetKey.in(shadowScope).value
    )

  def shadowInputKeys[T](
      configs: Seq[ConfigKey],
      tasks: Seq[AttributeKey[_]],
      keys: Seq[InputKey[T]]
  ): ShadowyProject =
    shadowScopedKeys(configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
      targetKey.in(originalScope) := targetKey.in(shadowScope).evaluated
    )

  def modify(newTrans: SettingTransformer): ShadowyProject =
    new ShadowyProject(thisProject, shadowee, trans + newTrans, settingOverrides)

  def settings(set: Setting[_]*): ShadowyProject =
    new ShadowyProject(thisProject, shadowee, trans, settingOverrides ++ set)

  def light: Project =
    thisProject
      .settings(
        (shadowee: ProjectDefinition[_]).settings.flatMap(trans.transform(_).newSettings)
      )
      .settings(settingOverrides)

  // In case you want to re-apply the same Ops to multiple ShadowyProjects.
  def flash(ops: ShadowyProject.Ops): Project = ops(this).light
}
