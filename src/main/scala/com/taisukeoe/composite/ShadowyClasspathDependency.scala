package com.taisukeoe.composite

import sbt._

class ShadowyClasspathDependency(
    val project: Either[Project, ShadowyProject],
    val configuration: Option[String]
)

object ShadowyClasspathDependency {
  final class Constructor(project: Either[Project, ShadowyProject]) {
    def %(conf: Configuration): ShadowyClasspathDependency = %(conf.name)

    def %(conf: String): ShadowyClasspathDependency =
      new ShadowyClasspathDependency(project, Some(conf))
  }
}
