package sbtshadowprojects

import sbt._
import sbt.Keys._

object ShadowProjectsPlugin extends AutoPlugin {

  object autoImport {
    type SettingTransformer = dev.taisukeoe.SettingTransformer
    val SettingTransformer = dev.taisukeoe.SettingTransformer
    val PredefTransformer = dev.taisukeoe.PredefTransformer

    implicit def prj2shadow(proj: Project): ShadowProject = new ShadowProject(proj, SettingTransformer.Empty)

    class ShadowProject(project: Project, mod: SettingTransformer) {
      def modify(trans: SettingTransformer => SettingTransformer): ShadowProject = new ShadowProject(project, trans(mod))

      def shadow(shadoweeProj: Project): Project =
        project
          .settings(
            (shadoweeProj: ProjectDefinition[_]).settings
              .flatMap(mod.transform(_).newSettings))
          .settings(
            for {
              cfg <- Seq(Compile, Test, Runtime)
              ky <- Seq(
                sources,
                resources,
              )
            } yield cfg / ky := (shadoweeProj / cfg / ky).value
          )
    }
  }
}
