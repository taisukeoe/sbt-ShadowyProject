package com.taisukeoe.composite

import java.io.File

import sbt.CompositeProject
import sbt.Keys._
import sbt.Project
import sbt._

final class ShadowyProject(id: String, val projects: Map[ShadowyProject.Type, Project], shadowy: ShadowyContext)
    extends CompositeProject {
  override def componentProjects: Seq[Project] = projects.values.toSeq

  def dependsOn(deps: ShadowyClasspathDependency*): ShadowyProject = {
    val dependenciesByType =
      deps.toSeq
        .flatMap { dep =>
          dep.project match {
            case Right(shadowyDep) =>
              shadowyDep.projects.map { case (typ, prj) => typ -> ClasspathDependency(prj, dep.configuration) }
            case Left(projectDep) =>
              ShadowyProject.Type.all.map(_ -> ClasspathDependency(projectDep, dep.configuration))
          }
        }
        .groupBy(_._1)
        .mapValues(_.map(_._2))

    mapProjectsByType((typ, project) => project.dependsOn(dependenciesByType(typ): _*))
  }

  def aggregates(deps: ShadowyAggregationReference*): ShadowyProject = {
    val aggregationByType =
      deps.toSeq
        .flatMap { dep =>
          dep.project match {
            case Right(shadowyDep) =>
              shadowyDep.projects
            case Left(projectDep) =>
              ShadowyProject.Type.all.map(_ -> projectDep)
          }
        }
        .groupBy(_._1)
        .mapValues(_.map(v => Project.projectToLocalProject(v._2)))

    mapProjectsByType((typ, project) => project.aggregate(aggregationByType(typ): _*))
  }

  def configure(transforms: (Project => Project)*): ShadowyProject =
    transform(_.configure(transforms: _*))

  def disablePlugins(ps: AutoPlugin*): ShadowyProject =
    transform(_.disablePlugins(ps: _*))

  def enablePlugins(ns: Plugins*): ShadowyProject =
    transform(_.enablePlugins(ns: _*))

  def configurePlatforms(types: ShadowyProject.Type*)(f: Project => Project): ShadowyProject = {

    val updatedProjects =
      types.foldLeft(projects)((acc, platform) => acc.updated(platform, f(acc(platform))))

    new ShadowyProject(id, updatedProjects, shadowy)
  }

  def in(dir: File): ShadowyProject =
    mapProjectsByType((typ, project) => project.in(shadowy.baseFor(typ, dir)))

  private def mapProjectsByType(f: (ShadowyProject.Type, Project) => Project): ShadowyProject = {
    val updatedProjects = projects.map {
      case (typ, project) => typ -> f(typ, project)
    }
    new ShadowyProject(id, updatedProjects, shadowy)
  }

  private def transform(f: Project => Project): ShadowyProject =
    mapProjectsByType((_, project) => f(project))

  private def transformByType(ThisType: ShadowyProject.Type)(f: Project => Project): ShadowyProject =
    mapProjectsByType {
      case (ThisType, project) => f(project)
      case (_, project) => project
    }

  def settings(setting: Def.SettingsDefinition*): ShadowyProject =
    mapProjectsByType((typ, project) => project.settings(shadowy.settingsFor(typ, setting.flatMap(_.settings))))
}
object ShadowyProject {
  final class Builder(id: String, commonBase: File, shadowy: ShadowyContext) {

    def build(): ShadowyProject = {

      val primary = Project(
        shadowy.idFor(Primary, id),
        shadowy.baseFor(Primary, commonBase)
      ).settings(
        name := id
      )

      val secondary = Project(
        shadowy.idFor(Secondary, id),
        shadowy.baseFor(Secondary, commonBase)
      ).settings(
        name := id,
        unmanagedSourceDirectories in Compile := unmanagedSourceDirectories.in(Compile).in(primary).value,
        unmanagedResourceDirectories in Compile := unmanagedResourceDirectories.in(Compile).in(primary).value,
        unmanagedSourceDirectories in Test := unmanagedSourceDirectories.in(Test).in(primary).value,
        unmanagedResourceDirectories in Test := unmanagedResourceDirectories.in(Test).in(primary).value
      )

      val map = Map(Primary -> primary, Secondary -> secondary)

      new ShadowyProject(id, map.map { case (typ, prj) => typ -> shadowy.configurationsFor(typ, prj) }, shadowy)
    }
  }

  // scalafix:off DisableSyntax.implicitConversion
  object Builder {
    import scala.language.implicitConversions
    final implicit def shadowyProjectFromBuilder(builder: ShadowyProject.Builder): ShadowyProject = {
      builder.build()
    }
  }
  // scalafix:on DisableSyntax.implicitConversion

  sealed abstract class Type
  case object Primary extends Type
  case object Secondary extends Type

  object Type {
    val all: Seq[Type] = Seq(Primary, Secondary)
  }

  def apply(id: String, base: File)(shadowy: ShadowyContext): Builder =
    new Builder(id, base, shadowy)
}
