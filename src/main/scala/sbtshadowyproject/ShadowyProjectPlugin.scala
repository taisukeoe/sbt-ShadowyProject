package sbtshadowyproject

import java.nio.file.Files

import scala.collection.JavaConverters._

import sbt._

import com.taisukeoe
import com.taisukeoe.Shade
import com.taisukeoe.Shadow
import com.taisukeoe.ShadowyProject
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
        ).keepConsistencyAt(PC.Configs: _*)

      def shade(shadowee: Project): Shade =
        new Shade(
          shadower,
          shadowee,
          Nil
        ).keepConsistencyAt(PC.Configs: _*)
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

    implicit class ShadowyOps[Shadowy](shadowy: Shadowy)(implicit EV: ShadowyProject[Shadowy]) {
      def keepConsistencyAt(configs: ConfigKey*): Shadowy = {
        /*
         * Since resourceGenerators in sbt plugin project lead to compile,
         * resourceGenerators and managedResources in original project scope must be independent from shadowy projects.
         *
         * Instead, all files under sourceManaged or resourceManaged directories are captured to shadowy managedSources or managedResources respectively.
         */
        val managedSettings: Seq[Seq[Setting[_]]] = for {
          c <- if (configs.nonEmpty) configs.map(Select(_)) else Seq(This)
          (
            managedSourcesOrResources,
            managedSourceOrResourceDirectories,
            sourceOrResourceManaged
          ) <- (
              PC.TaskKeysForManagedFiles,
              PC.SettingKeysForManagedDirs,
              PC.SettingKeysForManagedDir
          ).zipped
        } yield Seq(
          managedSourceOrResourceDirectories.in(Scope(This, c, Zero, Zero)) +=
            sourceOrResourceManaged.in(Scope(Select(EV.originalOf(shadowy)), c, Zero, Zero)).value,
          managedSourcesOrResources.in(Scope(This, c, Zero, Zero)) ++= {
            val managedDir =
              sourceOrResourceManaged.in(Scope(Select(EV.originalOf(shadowy)), c, Zero, Zero)).value
            if (managedDir.exists)
              for {
                path <- Files.walk(managedDir.toPath).iterator().asScala.toSeq
                file = path.toFile
                if file.isFile
              } yield file
            else
              Nil
          }
        )

        // sbt-plugin projects have resource generators, which may cause resource files duplication in ShadowyProject.
        val emptyGenerators: Seq[Setting[_]] = for {
          c <- if (configs.nonEmpty) configs.map(Select(_)) else Seq(This)
          sourceOrResourceGenerators <- PC.SettingKeysForGenerators
        } yield sourceOrResourceGenerators.in(Scope(This, c, Zero, Zero)) := Nil

        EV.reflectSettingKeys(shadowy, configs, Nil, PC.SettingKeysForUnmanagedDir)
          .reflectSettingKeys(configs, Nil, PC.SettingKeysForUnmanagedDirs)
          .reflectTaskKeys(configs, Nil, PC.TaskKeysForClasspath)
          .reflectTaskKeys(configs, Nil, PC.TaskKeysForUnmanagedFiles)
          .settings(managedSettings.flatten: _*)
          .settings(emptyGenerators: _*)
      }

      def reflectSettingKeys[T](
          configs: Seq[ConfigKey],
          tasks: Seq[AttributeKey[_]],
          keys: Seq[SettingKey[T]]
      ): Shadowy = EV.reflectSettingKeys(shadowy, configs, tasks, keys)

      def reflectTaskKeys[T](
          configs: Seq[ConfigKey],
          tasks: Seq[AttributeKey[_]],
          keys: Seq[TaskKey[T]]
      ): Shadowy = EV.reflectTaskKeys(shadowy, configs, tasks, keys)

      def reflectInputKeys[T](
          configs: Seq[ConfigKey],
          tasks: Seq[AttributeKey[_]],
          keys: Seq[InputKey[T]]
      ): Shadowy = EV.reflectInputKeys(shadowy, configs, tasks, keys)

      def settings(set: Setting[_]*): Shadowy = EV.settingsFor(shadowy, set)

      def light: Project = EV.light(shadowy)
    }
  }
}
