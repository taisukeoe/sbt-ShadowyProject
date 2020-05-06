package sbtshadowyproject

import sbt._

import com.taisukeoe
import com.taisukeoe.ProjectConsistency
import com.taisukeoe.ShadowyProject

object ShadowProjectsPlugin extends AutoPlugin {

  object autoImport {
    type SettingTransformer = taisukeoe.SettingTransformer
    val SettingTransformer = taisukeoe.SettingTransformer

    import SettingTransformer._

    implicit class ToShadowyProject(proj: Project) {
      import ProjectConsistency._

      def shadow(shadowee: Project): ShadowyProject =
        new ShadowyProject(
          proj,
          shadowee,
          /*
           * Capture shadowee task scoped settings like:
           *
           * Compile / SOME_TASK_KEY / sourceDirectories += baseDirectory.value / "SOME_DIR"
           */
          (
            RemoveTargetDir
              +: (DefaultSettingKeys ++ SupplementalSettingKeys).map(
                ShadowScopedSettingKey(shadowee, _)
              )
              ++: SupplementalTaskKeys.map(ShadowScopedTaskKey(shadowee, _))
          ).reduce(_ + _),
          Nil
        ).shadowSettingKeys(Seq(Compile, Test), Nil, DefaultSettingKeys)
    }
  }
}
