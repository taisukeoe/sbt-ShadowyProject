package com.taisukeoe

import sbt.Project
import sbt.Setting

class Shade(
    private val thisProject: Project,
    private val original: Project,
    private val settingOverrides: Seq[Setting[_]]
)

object Shade {
  implicit val shadowy: ShadowyProject[Shade] = new ShadowyProject[Shade] {
    override def originalOf(shadowy: Shade): Project = shadowy.original

    override def settingsFor(shadowy: Shade, set: Seq[Setting[_]]): Shade =
      new Shade(shadowy.thisProject, shadowy.original, shadowy.settingOverrides ++ set)

    override def light(shadowy: Shade): Project =
      shadowy.thisProject.settings(shadowy.settingOverrides)
  }
}
