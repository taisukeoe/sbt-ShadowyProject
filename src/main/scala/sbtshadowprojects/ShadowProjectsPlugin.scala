package sbtshadowprojects

import sbt.Keys._
import sbt._

import dev.taisukeoe.ScopeSelectable

object ShadowProjectsPlugin extends AutoPlugin {

  object autoImport {
    type SettingTransformer = dev.taisukeoe.SettingTransformer
    val SettingTransformer = dev.taisukeoe.SettingTransformer

    import SettingTransformer._

    import dev.taisukeoe.ScopeSelectable.Ops._

    implicit class ToShadowyProject(proj: Project) {
      private val defaultShadowSettingKeys = Seq(sourceDirectory, resourceDirectory, unmanagedBase)
      def shadow(shadowee: Project): ModifiableShadowyProject =
        new ModifiableShadowyProject(
          new ShadowyProject(
            proj,
            shadowee,
            (RemoveTargetSetting +: defaultShadowSettingKeys.map(ShadowSetting(shadowee, _)))
              .reduce(_ + _),
            Seq.empty
          ).shadowSettings(Seq(Compile, Test), defaultShadowSettingKeys)
        )
    }

    class ModifiableShadowyProject private[sbtshadowprojects] (shadow: ShadowyProject) {
      import shadow._
      // This is for advanced.
      def modifyMap(transform: Setting[_] => Seq[Setting[_]]): ShadowyProject =
        new ShadowyProject(
          proj.settings((shadowee: ProjectDefinition[_]).settings.flatMap(transform)),
          shadowee,
          SettingTransformer.RemoveAll,
          settingOverrides
        )

      // This is for most of use-cases.
      def modify(newTrans: SettingTransformer): ShadowyProject =
        new ShadowyProject(proj, shadowee, trans + newTrans, settingOverrides)

      def light: Project = shadow.light
    }

    //Use the constructor directly if you want to change above default arguments.
    class ShadowyProject(
        val proj: Project,
        val shadowee: Project,
        val trans: SettingTransformer,
        val settingOverrides: Seq[Setting[_]]
    ) {
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
