import sbt._
import sbt.Keys._
import _root_.scalafix.sbt.ScalafixPlugin.autoImport.{scalafixDependencies, scalafixSemanticdb}
import sbtcrossproject.CrossProject

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

object MyPlatform extends sbtcrossproject.Platform {
  def identifier: String                = "me"
  def sbtSuffix: String                 = "Me"
  def enable(project: Project): Project = project
}

object MyPlatformOps {
  implicit def MyCrossProjectBuilderOps(
                                         builder: CrossProject.Builder): MyCrossProjectOps =
    new MyCrossProjectOps(builder)

  implicit class MyCrossProjectOps(project: CrossProject) {
    def me: Project = project.projects(MyPlatform)

    def mySettings(ss: Def.SettingsDefinition*): CrossProject =
      myConfigure(_.settings(ss: _*))

    def myConfigure(transformer: Project => Project): CrossProject =
      project.configurePlatform(MyPlatform)(transformer)
  }
}
