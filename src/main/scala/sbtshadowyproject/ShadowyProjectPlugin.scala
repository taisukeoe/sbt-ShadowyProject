package sbtshadowyproject

import sbt._

import com.taisukeoe
import com.taisukeoe.Shade
import com.taisukeoe.Shadow
import com.taisukeoe.ShadowyProject
import com.taisukeoe.{ProjectConsistency => PC}

object ShadowyProjectPlugin extends AutoPlugin {

  object autoImport {
    type SettingTransformer = taisukeoe.SettingTransformer
    val SettingTransformer = taisukeoe.SettingTransformer

    import SettingTransformer._

    implicit class ToShadowyProject(shadower: Project) {

      def shadow(shadowee: Project): Shadow =
        new Shadow(
          shadower,
          shadowee,
          /*
           * Capture shadowee task scoped settings like:
           *
           * Compile / SOME_TASK_KEY / sourceDirectories += baseDirectory.value / "SOME_DIR"
           */
          (
            RemoveTargetDir
              +: PC.SettingKeys.map(ShadowScopedSettingKey(shadowee, _))
              ++: PC.TaskKeys.map(ShadowScopedTaskKey(shadowee, _))
          ).reduce(_ + _),
          Nil
        ).keepConsistencyAt(PC.Configs: _*)

      def shade(shadowee: Project): Shade =
        new Shade(
          shadower,
          shadowee,
          /*
           * Add SettingTransformers only for ProjectConsistency.
           * No need to add RemoveTargetDir becauqse original settings won't be copied in Shade.
           */
          (
            PC.SettingKeys.map(ShadowScopedSettingKey(shadowee, _))
              ++: PC.TaskKeys.map(ShadowScopedTaskKey(shadowee, _))
          ).reduce(_ + _),
          Nil
        ).keepConsistencyAt(PC.Configs: _*)
    }

    // Please be aware that autoShade and autoShadow can be called once each per a shadowee project, due to Project id collision.
    implicit class AutoShadowyProject(shadowee: Project) {
      def autoShade: Shade = {
        val id = s"shade${shadowee.id.capitalize}"
        val shadower = Project(id, file(s"shadowy/$id"))
        shadower.shade(shadowee)
      }

      def autoShadow: Shadow = {
        val id = s"shadow${shadowee.id.capitalize}"
        val shadower = Project(id, file(s"shadowy/$id"))
        shadower.shadow(shadowee)
      }
    }

    implicit class ShadowyOps[Shadowy](shadowy: Shadowy)(implicit EV: ShadowyProject[Shadowy]) {
      def keepConsistencyAt(configs: ConfigKey*): Shadowy =
        EV.reflectSettingKeys(shadowy, configs, Nil, PC.SettingKeysForDir)
          .reflectSettingKeys(configs, Nil, PC.SettingKeysForFiles)
          .reflectSettingKeys(configs, Nil, PC.SettingKeysForGenerators)
          .reflectTaskKeys(configs, Nil, PC.TaskKeysForClasspath)
          .reflectTaskKeys(configs, Nil, PC.TaskKeysForFiles)

      def reflectSettingKeys[T](
          configs: Seq[ConfigKey],
          tasks: Seq[AttributeKey[_]],
          keys: Seq[SettingKey[T]]
      ): Shadowy = EV.reflectSettingKeys(shadowy, configs, tasks, keys)

      def reflectTaskKeys[T](
          configs: Seq[ConfigKey],
          tasks: Seq[AttributeKey[_]],
          keys: Seq[TaskKey[T]]
      ): Shadowy = EV.reflectTaskKeys(shadowy, configs, tasks, keys)

      def reflectInputKeys[T](
          configs: Seq[ConfigKey],
          tasks: Seq[AttributeKey[_]],
          keys: Seq[InputKey[T]]
      ): Shadowy = EV.reflectInputKeys(shadowy, configs, tasks, keys)

      def settings(set: Setting[_]*): Shadowy = EV.settingsFor(shadowy, set)

      def light: Project = EV.light(shadowy)
    }
  }
}
