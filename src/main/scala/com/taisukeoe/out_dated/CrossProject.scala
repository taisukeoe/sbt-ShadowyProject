package com.taisukeoe.out_dated

/*
Copied and pasted from sbt-crossproject.

https://github.com/portable-scala/sbt-crossproject

----
Copyright (c) 2016, Denys Shabalin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

 * Neither the name of sbt-crossproject-project nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import scala.language.implicitConversions

import sbt.Keys._
import sbt._

// scalafix:off DisableSyntax.implicitConversion
final class CrossProject private[out_dated](
    private val id: String,
    val projects: Map[Platform, Project]
) extends CompositeProject {

  // CompositeProject API
  override def componentProjects: Seq[Project] = projects.valuesIterator.toSeq

  def aggregate(refs: CrossProject*): CrossProject = {
    val aggregatesByPlatform =
      refs.toSeq.flatMap(_.projects).groupBy(_._1).mapValues(_.map(_._2))

    mapProjectsByPlatform((platform, project) =>
      project.aggregate(aggregatesByPlatform(platform).map(p => p: ProjectReference): _*)
    )
  }

  def dependsOn(deps: CrossClasspathDependency*): CrossProject = {
    requireDependencies(deps.toList.map(_.project))

    val dependenciesByPlatform =
      deps.toSeq
        .flatMap(dep =>
          dep.project.projects.map {
            case (platform, project) =>
              platform -> ClasspathDependency(project, dep.configuration)
          }
        )
        .groupBy(_._1)
        .mapValues(_.map(_._2))

    mapProjectsByPlatform((platform, project) => project.dependsOn(dependenciesByPlatform(platform): _*))
  }

  def configs(cs: Configuration*): CrossProject =
    transform(_.configs(cs: _*))

  def configureCross(transforms: (CrossProject => CrossProject)*): CrossProject =
    transforms.foldLeft(this)((p, t) => t(p))

  def configure(transforms: (Project => Project)*): CrossProject =
    transform(_.configure(transforms: _*))

  def configurePlatform(platforms: Platform*)(f: Project => Project): CrossProject =
    configurePlatforms(platforms: _*)(f)

  def configurePlatforms(platforms: Platform*)(f: Project => Project): CrossProject = {

    val updatedProjects =
      platforms.foldLeft(projects)((acc, platform) => acc.updated(platform, f(acc(platform))))

    new CrossProject(id, updatedProjects)
  }

  def disablePlugins(ps: AutoPlugin*): CrossProject =
    transform(_.disablePlugins(ps: _*))

  def enablePlugins(ns: Plugins*): CrossProject =
    transform(_.enablePlugins(ns: _*))

  def in(dir: File): CrossProject =
    mapProjectsByPlatform((platform, project) => project.in(CrossType.Pure.platformDir(dir, platform)))

  def overrideConfigs(cs: Configuration*): CrossProject =
    transform(_.overrideConfigs(cs: _*))

  def settings(ss: Def.SettingsDefinition*): CrossProject =
    transform(_.settings(ss: _*))

  def platformsSettings(platforms: Platform*)(ss: Def.SettingsDefinition*): CrossProject =
    configurePlatforms(platforms: _*)(_.settings(ss: _*))

  override def toString(): String =
    projects
      .map {
        case (platform, project) =>
          s"${platform.identifier} = $project"
      }
      .mkString("CrossProject(", ",", ")")

  private def platforms = projects.keySet

  private def mapProjectsByPlatform(f: (Platform, Project) => Project): CrossProject = {
    val updatedProjects = projects.map {
      case (platform, project) => platform -> f(platform, project)
    }
    new CrossProject(id, updatedProjects)
  }

  private def transform(f: Project => Project): CrossProject =
    mapProjectsByPlatform((platform, project) => f(project))

  private def requireDependencies(refs: List[CrossProject]): Unit = {
    for (ref <- refs) {
      val missings = platforms -- ref.platforms
      if (missings.nonEmpty) {
        throw new IllegalArgumentException(
          s"The cross-project ${this.id} cannot depend on ${ref.id} because " +
            "the latter lacks some platforms of the former: " +
            missings.mkString(", ")
        )
      }
    }
  }
}

object CrossProject {
  final class Builder private[CrossProject] (
      id: String,
      base: File,
      platforms: Seq[Platform]
  ) {
    private[CrossProject] def this(id: String, base: File, platforms: Seq[Platform], internal: Boolean) =
      this(id, base, platforms)

    def build(): CrossProject = {
      val crossType = CrossType.Pure
      val sharedSrc = sharedSrcSettings(crossType)

      val projects =
        platforms.map { platform =>
          val projectID =
            if (platform == Primary) id
            else id + platform.sbtSuffix

          platform -> platform.enable(
            Project(
              projectID,
              crossType.platformDir(base, platform)
            ).settings(
              CrossPlugin.autoImport.crossProjectPlatform := platform,
              name := id, // #80
              sharedSrc
            )
          )
        }.toMap

      new CrossProject(id, projects)
    }

    private def sharedSrcSettings(crossType: CrossType): Seq[Setting[_]] = {
      def makeCrossSources(sharedSrcDir: Option[File], scalaBinaryVersion: String, cross: Boolean): Seq[File] = {
        sharedSrcDir match {
          case Some(dir) =>
            if (cross)
              Seq(dir.getParentFile / s"${dir.name}-$scalaBinaryVersion", dir)
            else
              Seq(dir)
          case None => Seq()
        }
      }

      Seq(
        unmanagedSourceDirectories in Compile ++= {
          makeCrossSources(
            crossType.sharedSrcDir(baseDirectory.value, "main"),
            scalaBinaryVersion.value,
            crossPaths.value
          )
        },
        unmanagedSourceDirectories in Test ++= {
          makeCrossSources(
            crossType.sharedSrcDir(baseDirectory.value, "test"),
            scalaBinaryVersion.value,
            crossPaths.value
          )
        }
      )
    }
  }

  object Builder {
    final implicit def crossProjectFromBuilder(builder: CrossProject.Builder): CrossProject = {
      builder.build()
    }
  }

  def apply(id: String, base: File)(platforms: Platform*): Builder =
    new Builder(id, base, platforms, internal = true)
}
