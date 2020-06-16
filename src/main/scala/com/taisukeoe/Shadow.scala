package com.taisukeoe

import sbt.Project
import sbt.ProjectDefinition
import sbt.ProjectReference
import sbt.Setting

//Use the constructor directly if you want to change above default arguments.
class Shadow(
    private val thisProject: Project,
    private val original: Project,
    private val trans: SettingTransformer,
    private val settingOverrides: Seq[Setting[_]]
) {
  def modify(newTrans: SettingTransformer): Shadow =
    new Shadow(thisProject, original, trans + newTrans, settingOverrides)

  // In case you want to re-apply the same Ops to multiple ShadowyProjects.
  def flash(ops: Shadow.Ops): Project = implicitly[ShadowyProject[Shadow]].light(ops(this))
}

object Shadow {
  type Ops = Shadow => Shadow
  /*
   * Just a shorthand method to define an ops like:
   *
   * lazy val ops = ShadowyProject.ops(_.modify(RemoveXFatalWarnings))
   */
  def ops(f: Ops): Ops = f

  implicit val shadowy: ShadowyProject[Shadow] = new ShadowyProject[Shadow] {
    override def originalOf(shadowy: Shadow): Project = shadowy.original

    override def settingsFor(set: Seq[Setting[_]])(shadowy: Shadow): Shadow =
      new Shadow(
        shadowy.thisProject,
        shadowy.original,
        shadowy.trans,
        shadowy.settingOverrides ++ set
      )

    override def light(shadowy: Shadow): Project =
      shadowy.thisProject
        .settings(
          (shadowy.original: ProjectDefinition[_]).settings
            .flatMap(shadowy.trans.transform(_).newSettings)
        )
        .settings(shadowy.settingOverrides)
        .dependsOn(shadowy.original.dependencies: _*)
        .aggregate((shadowy.original.aggregate: Seq[ProjectReference]): _*)
  }
}
