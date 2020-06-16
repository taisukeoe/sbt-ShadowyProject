package sbtshadowyproject

import sbt.Keys._
import sbt._

import com.taisukeoe
import com.taisukeoe.Shade
import com.taisukeoe.Shadow
import com.taisukeoe.ShadowyProject
import com.taisukeoe.internal.Parser
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
           * Copying sources, resources or jars related keys may lead to NullPointerException and should be avoided.
           * In case a shadowee project has `Configuration / Task / unmanagedSources` kind of settings,
           *
           */
          RemoveTargetDir
            + ExcludeKeyNames(PC.AllSettingKeys.map(_.key.label).toSet)
            + ExcludeKeyNames(PC.AllTaskKeys.map(_.key.label).toSet),
          Nil
        ).isConsistentAt(PC.Configs: _*)

      private case class ProjectConfigDependencies(from: Configuration, to: Configuration, dependent: ProjectRef) {
        def taskKey[T](key: TaskKey[Seq[T]]): TaskKey[Seq[T]] = key.in(dependent, to)
        def at(given: Configuration): Option[ProjectConfigDependencies] = if (from == given) Some(this) else None
      }

      private def recurProjectConfigDependencies(
          classpathDep: ClasspathDep[ProjectRef],
          originalFrom: Option[Configuration],
          upstreamTo: Option[Configuration],
          depMap: Map[ProjectRef, Seq[ClasspathDep[ProjectRef]]]
      ): Seq[ProjectConfigDependencies] = {
        val configMap = classpathDep.configuration.map(Parser.configs.parse).getOrElse(Seq(Compile -> Compile))

        configMap.collect {
          // Option#contains is not available in Scala 2.10
          case (from, to) if upstreamTo.isEmpty || upstreamTo.exists(_ == from) =>
            ProjectConfigDependencies(originalFrom.getOrElse(from), to, classpathDep.project)
        } ++: {
          for {
            classpathDep <- depMap.getOrElse(classpathDep.project, Nil)
            (from, to) <- configMap
          } yield recurProjectConfigDependencies(classpathDep, originalFrom.orElse(Some(from)), Some(to), depMap)
        }.flatten
      }

      def deepShadow(shadowee: Project, at: Seq[Configuration] = PC.Configurations): Shadow =
        new Shadow(
          shadower,
          shadowee,
          RemoveTargetDir
            + ExcludeKeyNames(PC.AllSettingKeys.map(_.key.label).toSet)
            + ExcludeKeyNames(PC.AllTaskKeys.map(_.key.label).toSet),
          at.map { cfg =>
            sources.in(cfg) ++= Def.taskDyn {
              val depMap = buildDependencies.value.classpath
              val shadoweeRef = thisProjectRef.in(shadowee).value

              depMap
                .getOrElse(shadoweeRef, Nil)
                .flatMap(
                  recurProjectConfigDependencies(_, None, None, depMap).flatMap(_.at(cfg)).map(_.taskKey(sources))
                )
                .join
                .map(_.flatten)
            }.value
          }
        ).isConsistentAt(at.map(ConfigKey.configurationToKey): _*)

      def shade(shadowee: Project): Shade =
        new Shade(
          shadower,
          shadowee,
          Nil
        ).isConsistentAt(PC.Configs: _*)
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

    implicit class ShadowyOps[Shadowy: ShadowyProject](override val shadowy: Shadowy)
        extends ShadowyProject.Ops[Shadowy]
  }
}
