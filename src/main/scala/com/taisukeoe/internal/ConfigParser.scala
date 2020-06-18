package com.taisukeoe.internal

import sbt._

class ConfigParser(targets: Seq[Configuration]) {

  lazy val mapping: Map[String, Configuration] = targets.map(c => c.name -> c).toMap

  def defaultConfig: String = "compile"

  def parse(configuration: String): Seq[(Configuration, Configuration)] = {
    configuration
      .split(";")
      .toSeq
      .view
      .map(_.split("->", 2))
      .map {
        case Array(from) => from -> defaultConfig
        case Array(from, to) => from -> to
      }
      .flatMap {
        case (from, to) => from.split(",").flatMap(f => to.split(",").map(f -> _))
      }
      .flatMap {
        case ("*", "*") =>
          for {
            t1 <- targets
            t2 <- targets
          } yield t1 -> t2
        case ("*", to) =>
          mapping.get(to).toSeq.flatMap(t => targets.map(_ -> t))
        case (from, "*") =>
          mapping.get(from).toSeq.flatMap(f => targets.map(f -> _))
        case (from, to) =>
          mapping.get(from).flatMap { f =>
            mapping.get(to).map(f -> _)
          }
      }
      .toList
  }
}

object Parser {
  lazy val predefConfigs: Seq[Configuration] =
    Seq(Default, Compile, IntegrationTest, Provided, Runtime, Test, Optional)
  lazy val configs = new ConfigParser(predefConfigs)
}
