package sbtshadowyproject

import sbt._

import com.taisukeoe
import com.taisukeoe.ProjectConsistency

object ShadowProjectsPlugin extends AutoPlugin {

  object autoImport {
    type SettingTransformer = taisukeoe.SettingTransformer
    val SettingTransformer = taisukeoe.SettingTransformer

    import SettingTransformer._

    implicit class ToShadowyProject(proj: Project) {
      import ProjectConsistency._

      def shadow(shadowee: Project): ShadowyProject =
        new ShadowyProject(
          proj,
          shadowee,
          /*
           * Capture shadowee task scoped settings like:
           *
           * Compile / SOME_TASK_KEY / sourceDirectories += baseDirectory.value / "SOME_DIR"
           */
          (
            RemoveTargetDir
              +: (DefaultSettingKeys ++ SupplementalSettingKeys).map(
                ShadowScopedSettingKey(shadowee, _)
              )
              ++: SupplementalTaskKeys.map(ShadowScopedTaskKey(shadowee, _))
          ).reduce(_ + _),
          Nil
        ).shadowSettingKeys(Seq(Compile, Test), Nil, DefaultSettingKeys)
    }

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
  }
}
