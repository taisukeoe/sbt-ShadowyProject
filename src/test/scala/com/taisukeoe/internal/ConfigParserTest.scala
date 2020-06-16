package com.taisukeoe.internal

import sbt._

import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.prop.TableDrivenPropertyChecks

class ConfigParserTest extends AsyncFunSuite with TableDrivenPropertyChecks {

  private val fractions = Table[String, Seq[(Configuration, Configuration)]](
    ("configuration", "pairs"),
    ("test", Seq(Test -> Compile)),
    ("compile->compile", Seq(Compile -> Compile)),
    ("compile->compile;test->test", Seq(Compile -> Compile, Test -> Test)),
    ("compile->compile,test,runtime", Seq(Compile -> Compile, Compile -> Test, Compile -> Runtime))
  )

  test("ConfigParser parses configuration properly") {
    forAll(fractions) { (cfg, pairs) =>
      assert(Parser.configs.parse(cfg) == pairs)
    }
  }
}
