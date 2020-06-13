package sbtshadowyproject

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
        ).isConsistentAt(PC.Configs, Nil)

      def shade(shadowee: Project): Shade =
        new Shade(
          shadower,
          shadowee,
          Nil
        ).isConsistentAt(PC.Configs, Nil)
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
