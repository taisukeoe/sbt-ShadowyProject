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
            (RemoveTargetDir +: defaultShadowSettingKeys.map(
              ShadowScopedSettingKey(shadowee, _)
            )).reduce(_ + _),
            Seq.empty
          ).shadowSettingKeys(Seq(Compile, Test), defaultShadowSettingKeys)
        )
    }

    class ModifiableShadowyProject private[sbtshadowprojects] (shadow: ShadowyProject) {
      import shadow._
      // This is for advanced.
      def modifyMap(transform: Setting[_] => Seq[Setting[_]]): ShadowyProject =
        new ShadowyProject(
          thisProject.settings((shadowee: ProjectDefinition[_]).settings.flatMap(transform)),
          shadowee,
          SettingTransformer.RemoveAll,
          settingOverrides
        )

      // This is for most of use-cases.
      def modify(newTrans: SettingTransformer): ShadowyProject =
        new ShadowyProject(thisProject, shadowee, trans + newTrans, settingOverrides)

      def light: Project = shadow.light
    }

    //Use the constructor directly if you want to change above default arguments.
    class ShadowyProject(
        val thisProject: Project,
        val shadowee: Project,
        val trans: SettingTransformer,
        val settingOverrides: Seq[Setting[_]]
    ) {
      def shadowSettingKeys[Axis: ScopeSelectable, T](
          axes: Seq[Axis],
          keys: Seq[SettingKey[T]]
      ): ShadowyProject = {
        val newOverrides = for {
          axis <- axes
          targetKey <- keys
        } yield targetKey.in(axis.asScope) := targetKey.in(axis.asScope).in(shadowee).value

        new ShadowyProject(
          thisProject,
          shadowee,
          trans,
          settingOverrides ++ newOverrides
        )
      }

      def shadowTaskKeys[Axis: ScopeSelectable, T](
          axes: Seq[Axis],
          keys: Seq[TaskKey[T]]
      ): ShadowyProject = {
        val newOverrides = for {
          axis <- axes
          targetKey <- keys
        } yield targetKey.in(axis.asScope) := targetKey.in(axis.asScope).in(shadowee).value

        new ShadowyProject(
          thisProject,
          shadowee,
          trans,
          settingOverrides ++ newOverrides
        )
      }

      def shadowInputKeys[Axis: ScopeSelectable, T](
          axes: Seq[Axis],
          keys: Seq[InputKey[T]]
      ): ShadowyProject = {
        val newOverrides = for {
          axis <- axes
          targetKey <- keys
        } yield targetKey.in(axis.asScope) := targetKey.in(axis.asScope).in(shadowee).evaluated

        new ShadowyProject(
          thisProject,
          shadowee,
          trans,
          settingOverrides ++ newOverrides
        )
      }

      def light: Project =
        thisProject
          .settings(
            (shadowee: ProjectDefinition[_]).settings.flatMap(trans.transform(_).newSettings)
          )
          .settings(settingOverrides)
    }
  }
}
