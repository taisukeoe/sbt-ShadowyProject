package com.taisukeoe.composite

import scala.language.experimental.macros
import scala.language.implicitConversions

import sbt._

import com.taisukeoe.composite.ShadowyProject.Type

object ShadowyPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {

    type ShadowyContext = com.taisukeoe.composite.ShadowyContext

    def shadowyProject(shadowy: ShadowyContext): ShadowyProject.Builder =
      macro ShadowyProjectMacros.shadowyProject_impl

    implicit class shadowyProjectOps(val shadowyProj: ShadowyProject) {
      def primary: Project = shadowyProj.projects(ShadowyProject.Primary)
      def secondary: Project = shadowyProj.projects(ShadowyProject.Secondary)
    }
    // scalafix:off DisableSyntax.implicitConversion
    final implicit def toShadowyClasspathDependencyConstructor(
        sp: ShadowyProject
    ): ShadowyClasspathDependency.Constructor =
      new ShadowyClasspathDependency.Constructor(Right(sp))

    final implicit def toShadowyClasspathDependency(sp: ShadowyProject): ShadowyClasspathDependency =
      new ShadowyClasspathDependency(Right(sp), None)

    final implicit def prjToShadowyClasspathDependencyConstructor(
        prj: Project
    ): ShadowyClasspathDependency.Constructor =
      new ShadowyClasspathDependency.Constructor(Left(prj))

    final implicit def prjToShadowyClasspathDependency(prj: Project): ShadowyClasspathDependency =
      new ShadowyClasspathDependency(Left(prj), None)

    final implicit def ToShadowyAggregationReference(sp: ShadowyProject): ShadowyAggregationReference =
      new ShadowyAggregationReference(Right(sp))

    final implicit def prjToShadowyAggregationReference(prj: Project): ShadowyAggregationReference =
      new ShadowyAggregationReference(Left(prj))
    // scalafix:on DisableSyntax.implicitConversion

    sealed class ForType(targetType: ShadowyProject.Type) {
      def disablePlugins(plugins: AutoPlugin*): ProjectTransformer =
        new ProjectTransformer {
          override def target: Type = targetType
          override def transform(project: Project): Project = project.disablePlugins(plugins: _*)
        }
      def settings(setting: Def.SettingsDefinition): ProjectTransformer =
        new ProjectTransformer {
          override def target: Type = targetType
          override def transform(project: Project): Project = project.settings(setting)
        }
      def settings(setting: Seq[Def.Setting[_]]): ProjectTransformer =
        new ProjectTransformer {
          override def target: Type = targetType
          override def transform(project: Project): Project = project.settings(setting)
        }
    }
    object ForPrimary extends ForType(ShadowyProject.Primary)

    object ForSecondary extends ForType(ShadowyProject.Secondary)
  }
}
