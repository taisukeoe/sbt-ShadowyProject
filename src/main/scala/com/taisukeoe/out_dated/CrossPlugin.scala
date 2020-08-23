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

import scala.language.experimental.macros
import scala.language.implicitConversions

import sbt._

// scalafix:off DisableSyntax.implicitConversion
object CrossPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {

    type CrossType = com.taisukeoe.out_dated.CrossType
    val CrossType = com.taisukeoe.out_dated.CrossType

    // TODO:
    //  refactoring(modifiers).settings( /* .. */ )
    //  という感じの構文で、適切な設定のrefactoring sub projectが自動生成されるようにする
    //
    // TODO:
    //  それとは別に、refactoringSettings() や　primarySettings()
    //  も使えるようにする
    def refactoringProject(platforms: Platform*): CrossProject.Builder =
      macro CrossProjectMacros.vargCrossProject_impl

    // Cross-classpath dependency builders

    final implicit def toCrossClasspathDependencyConstructor(cp: CrossProject): CrossClasspathDependency.Constructor =
      new CrossClasspathDependency.Constructor(cp)

    final implicit def toCrossClasspathDependency(cp: CrossProject): CrossClasspathDependency =
      new CrossClasspathDependency(cp, None)

    implicit class ShadowLikeProject(prj: Project) {
      import com.taisukeoe.SettingTransformer
      def modify(trans: SettingTransformer): Project =
        Project(prj.id, prj.base)
          .settings((prj.settings: Seq[Setting[_]]).flatMap(trans.transform(_).newSettings))
          .aggregate((prj.aggregate: Seq[ProjectReference]): _*)
          .dependsOn(prj.dependencies: _*)
          .enablePlugins(prj.plugins)
    }

    // The JVM platform

    val Primary = com.taisukeoe.out_dated.Primary
    val Refactoring = com.taisukeoe.out_dated.Refactoring

    implicit def PrimaryCrossProjectBuilderOps(builder: CrossProject.Builder): PrimaryCrossProjectOps =
      new PrimaryCrossProjectOps(builder)

    implicit class PrimaryCrossProjectOps(project: CrossProject) {
      def primary: Project = project.projects(Primary)

      def primarySettings(ss: Def.SettingsDefinition*): CrossProject =
        primaryConfigure(_.settings(ss: _*))

      def primaryConfigure(transformer: Project => Project): CrossProject =
        project.configurePlatform(Primary)(transformer)
    }

    implicit class RefactorCrossProjectOps(project: CrossProject) {
      def refactor: Project = project.projects(Refactoring)

      def refactorSettings(ss: Def.SettingsDefinition*): CrossProject =
        refactorConfigure(_.settings(ss: _*))

      def refactorConfigure(transformer: Project => Project): CrossProject =
        project.configurePlatform(Refactoring)(transformer)
    }

    lazy val crossProjectPlatform: SettingKey[Platform] =
      settingKey[Platform]("platform of the current project")

  }
}
