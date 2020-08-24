package com.taisukeoe.composite

import sbt._

import com.taisukeoe.SettingTransformer
import com.taisukeoe.composite.ShadowyProject._

class ShadowyContext(transformer: SettingTransformer, projectTransformer: ProjectTransformer*) {
  def idFor(typ: Type, baseId: String): String =
    typ match {
      case Primary => baseId
      case Secondary => baseId + "Shadow"
    }
  def baseFor(typ: Type, base: File): File =
    typ match {
      case Primary => base
      case Secondary => base / "shadow"
    }
  def settingsFor(typ: Type, settings: Seq[Def.Setting[_]]): Seq[Setting[_]] =
    typ match {
      case Primary => settings
      case Secondary => settings.flatMap(transformer.transform(_).newSettings)
    }
  def configurationsFor(typ: Type, project: Project): Project =
    projectTransformer.foldLeft(project)((prj, trans) => if (trans.target(typ)) trans.transform(prj) else prj)
}

trait ProjectTransformer {
  def target(typ: ShadowyProject.Type): Boolean
  def transform(project: Project): Project
}
