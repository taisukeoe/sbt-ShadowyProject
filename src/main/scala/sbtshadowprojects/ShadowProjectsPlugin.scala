package sbtshadowprojects

import dev.taisukeoe.ScopeSelectable
import sbt.Keys._
import sbt._

object ShadowProjectsPlugin extends AutoPlugin {

  object autoImport {
    type SettingTransformer = dev.taisukeoe.SettingTransformer
    val SettingTransformer = dev.taisukeoe.SettingTransformer

    import SettingTransformer._

    import dev.taisukeoe.ScopeSelectable.Ops._

    implicit class ToShadowyProject(proj: Project) {
      def shadow(shadowee: Project): ShadowyProject =
        new ShadowyProject(proj, shadowee, RemoveTarget, Seq.empty).shadowSettings(
          Seq(Compile, Test, Runtime),
          Seq(sourceDirectory, resourceDirectory, unmanagedBase)
        )
    }

    class ShadowyProject(
        proj: Project,
        shadowee: Project,
        trans: SettingTransformer,
        settingOverrides: Seq[Setting[_]]
    ) {
      def modify(newTrans: SettingTransformer): ShadowyProject =
        new ShadowyProject(proj, shadowee, trans && newTrans, settingOverrides)

      def shadowSettings[Axis: ScopeSelectable, T](
          axes: Seq[Axis],
          keys: Seq[SettingKey[T]]
      ): ShadowyProject = {
        val newOverrides = for {
          axis <- axes
          targetKey <- keys
        } yield targetKey.in(axis.asScope) := targetKey.in(axis.asScope).in(shadowee).value

        new ShadowyProject(proj, shadowee, trans, settingOverrides ++ newOverrides)
      }

      def shadowTasks[Axis: ScopeSelectable, T](
          axes: Seq[Axis],
          keys: Seq[TaskKey[T]]
      ): ShadowyProject = {
        val newOverrides = for {
          axis <- axes
          targetKey <- keys
        } yield targetKey.in(axis.asScope) := targetKey.in(axis.asScope).in(shadowee).value

        new ShadowyProject(proj, shadowee, trans, settingOverrides ++ newOverrides)
      }

      def shadowInputs[Axis: ScopeSelectable, T](
          axes: Seq[Axis],
          keys: Seq[InputKey[T]]
      ): ShadowyProject = {
        val newOverrides = for {
          axis <- axes
          targetKey <- keys
        } yield targetKey.in(axis.asScope) := targetKey.in(axis.asScope).in(shadowee).evaluated

        new ShadowyProject(proj, shadowee, trans, settingOverrides ++ newOverrides)
      }

      def light: Project =
        proj
          .settings(
            (shadowee: ProjectDefinition[_]).settings.flatMap(trans.transform(_).newSettings)
          )
          .settings(settingOverrides)
    }
  }
}
