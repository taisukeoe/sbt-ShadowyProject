import sbt._
import sbt.Keys._
import _root_.scalafix.sbt.ScalafixPlugin.autoImport.{scalafixDependencies, scalafixSemanticdb}

object ScalafixSettings {
  private val unused = "-Ywarn-unused"

  lazy val permanent: Seq[Setting[_]] = Seq(
    scalacOptions += unused,
    scalafixDependencies in ThisBuild ++= Seq(
      "com.github.vovapolu" %% "scaluzzi" % "0.1.4",
      "com.github.liancheng" %% "organize-imports" % "0.3.1-RC2"
    ),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  ) ++ Seq(Compile, Test).map(_ / console / scalacOptions -= unused)
}
