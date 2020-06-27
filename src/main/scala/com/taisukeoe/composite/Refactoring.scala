package com.taisukeoe.composite

import sbt._

case object Refactoring extends Platform {
  def identifier: String = "refactor"
  def sbtSuffix: String = "Refactor"
  def enable(project: Project): Project = project
}
