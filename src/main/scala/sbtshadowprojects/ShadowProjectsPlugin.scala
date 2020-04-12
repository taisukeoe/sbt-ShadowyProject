package sbtshadowprojects

import sbt.Keys._
import sbt._

object ShadowProjectsPlugin extends AutoPlugin {

  object autoImport {
    type SettingTransformer = dev.taisukeoe.SettingTransformer
    val SettingTransformer = dev.taisukeoe.SettingTransformer

    import SettingTransformer._

    implicit class ToShadowProject(proj: Project) {
      def modify(trans: SettingTransformer): ShadowProject =
        new ShadowProject(proj, RemoveTarget && trans)
    }

    class ShadowProject(project: Project, mod: SettingTransformer) {
      def shadow(shadoweeProj: Project): Project = {
        project
          .settings(
            (shadoweeProj: ProjectDefinition[_]).settings
              .flatMap(mod.transform(_).newSettings)
          )
          .settings(
            for {
              cfg <- Seq(Compile, Test, Runtime)
              targetKey <- Seq(
                sourceDirectory,
                resourceDirectory,
                unmanagedBase
              )
            } yield cfg / targetKey := (shadoweeProj / cfg / targetKey).value
          )
      }
    }
  }
}
