package dev.taisukeoe

import sbt.Keys.scalacOptions
import sbt.{ Def, Task }

trait SettingTransformerTestBase {
  import SettingTransformer._
  //scalafix:off DisableSyntax.valInAbstract
  val original: Def.Setting[Task[Seq[String]]] = scalacOptions ++= Seq("-Werror", "-Wunused")
  val removeWerror: Def.Setting[Task[Seq[String]]] = scalacOptions -= "-Werror"
  val addDeprecation: Def.Setting[Task[Seq[String]]] = scalacOptions += "-deprecation"

  val originalAlg: NoChange = NoChange(original)
  val removeWerrorAlg: Add = Add(original, Seq(removeWerror))
  val addDeprecationAlg: Add = Add(original, Seq(addDeprecation))
  //scalafix:on DisableSyntax.valInAbstract
}
