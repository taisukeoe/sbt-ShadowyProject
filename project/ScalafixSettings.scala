import sbt._
import sbt.Keys._
import _root_.scalafix.sbt.ScalafixPlugin.autoImport.{scalafixDependencies, scalafixSemanticdb}

object ScalafixSettings {
  private val unused = "-Ywarn-unused"

  lazy val permanent: Seq[Setting[_]] = Seq(
    scalacOptions ++= Seq(unused, "-Yrangepos"),
    scalafixDependencies in ThisBuild += "com.github.vovapolu" %% "scaluzzi" % "0.1.4",
    addCompilerPlugin(scalafixSemanticdb)
  ) ++ Seq(Compile, Test).map(_ / console / scalacOptions -= unused)
}
