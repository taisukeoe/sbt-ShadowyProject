package com.taisukeoe.internal

import sbt._

class ConfigParser[T](mapping: Map[String, T]) {

  def defaultConfig: String = "compile"

  def parse(configuration: String): Seq[(T, T)] = {
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
        case (from, to) =>
          mapping.get(from).flatMap { f =>
            mapping.get(to).map(f -> _)
          }
      }
      .seq
  }
}

object Parser {
  private lazy val predefConfigs: Seq[Configuration] =
    Seq(Default, Compile, IntegrationTest, Provided, Runtime, Test, Optional)
  lazy val configs = new ConfigParser[Configuration](predefConfigs.map(c => c.name -> c).toMap)
}
