package sbtshadowyproject

import sbt.Keys._
import sbt._

import com.taisukeoe

object ShadowProjectsPlugin extends AutoPlugin {

  object autoImport {
    type SettingTransformer = taisukeoe.SettingTransformer
    val SettingTransformer = taisukeoe.SettingTransformer

    import SettingTransformer._

    implicit class ToShadowyProject(proj: Project) {
      private val defaultShadowSettingKeys = Seq(sourceDirectory, resourceDirectory, unmanagedBase)
      def shadow(shadowee: Project): ShadowyProject =
        new ShadowyProject(
          proj,
          shadowee,
          (RemoveTargetDir +: defaultShadowSettingKeys.map(
            ShadowScopedSettingKey(shadowee, _)
          )).reduce(_ + _),
          Nil
        ).shadowSettingKeys(Seq(Compile, Test), Nil, defaultShadowSettingKeys)
    }

    //Use the constructor directly if you want to change above default arguments.
    class ShadowyProject(
        val thisProject: Project,
        val shadowee: Project,
        val trans: SettingTransformer,
        val settingOverrides: Seq[Setting[_]]
    ) {
      private def shadowKeys[KeyType](
          configs: Seq[ConfigKey],
          tasks: Seq[AttributeKey[_]],
          keys: Seq[KeyType]
      )(settingExp: (KeyType, Scope, Scope) => Setting[_]): ShadowyProject = {
        val newOverrides = for {
          c <- if (configs.nonEmpty) configs.map(Select(_)) else Seq(This)
          t <- if (tasks.nonEmpty) tasks.map(Select(_)) else Seq(This)
          targetKey <- keys
        } yield settingExp(targetKey, Scope(This, c, t, This), Scope(Select(shadowee), c, t, This))

        new ShadowyProject(
          thisProject,
          shadowee,
          trans,
          settingOverrides ++ newOverrides
        )
      }

      def shadowSettingKeys[T](
          configs: Seq[ConfigKey],
          tasks: Seq[AttributeKey[_]],
          keys: Seq[SettingKey[T]]
      ): ShadowyProject =
        shadowKeys(configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
          targetKey.in(originalScope) := targetKey.in(shadowScope).value
        )

      def shadowTaskKeys[T](
          configs: Seq[ConfigKey],
          tasks: Seq[AttributeKey[_]],
          keys: Seq[TaskKey[T]]
      ): ShadowyProject =
        shadowKeys(configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
          targetKey.in(originalScope) := targetKey.in(shadowScope).value
        )

      def shadowInputKeys[T](
          configs: Seq[ConfigKey],
          tasks: Seq[AttributeKey[_]],
          keys: Seq[InputKey[T]]
      ): ShadowyProject =
        shadowKeys(configs, tasks, keys)((targetKey, originalScope, shadowScope) =>
          targetKey.in(originalScope) := targetKey.in(shadowScope).evaluated
        )

      def modify(newTrans: SettingTransformer): ShadowyProject =
        new ShadowyProject(thisProject, shadowee, trans + newTrans, settingOverrides)

      def light: Project =
        thisProject
          .settings(
            (shadowee: ProjectDefinition[_]).settings.flatMap(trans.transform(_).newSettings)
          )
          .settings(settingOverrides)
    }
  }
}
