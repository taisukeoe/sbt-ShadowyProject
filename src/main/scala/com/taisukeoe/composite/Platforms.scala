package com.taisukeoe.composite

import sbt._

case object Refactoring extends Platform {
  def identifier: String = "refactor"
  def sbtSuffix: String = "Refactor"
  def enable(project: Project): Project = project
}

case object Primary extends Platform {
  def identifier: String = "primary"
  def sbtSuffix: String = "Primary"
  def enable(project: Project): Project = project
}
