package dev.taisukeoe

import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.prop.TableDrivenPropertyChecks

import sbt.Keys._

class SettingTransformerTest extends AsyncFunSuite with TableDrivenPropertyChecks {
  import SettingTransformer._

  val scala213 = scalaVersion := "2.13.1"
  val scala212 = scalaVersion := "2.12.11"

  sealed trait Operand
  case object Sum extends Operand
  case object Product extends Operand

  private val fractions = Table[Result, Operand, Result, Result](
    ("left", "operand", "right", "Result"),
    (NoChange(scala212), Sum, Removed, Removed),
    (NoChange(scala212), Product, Removed, Removed),
    (NoChange(scala212), Sum, Transformed(Seq(scala213)), Transformed(Seq(scala213))),
    (NoChange(scala212), Product, Transformed(Seq(scala213)), Transformed(Seq(scala213))),
    (NoChange(scala212), Sum, NoChange(scala212), NoChange(scala212)),
    (Transformed(Seq(scala213)), Sum, Removed, Transformed(Seq(scala213))),
    (Transformed(Seq(scala213)), Product, Removed, Removed),
    (Transformed(Seq(scala213)), Sum, NoChange(scala212), Transformed(Seq(scala213))),
    (Transformed(Seq(scala213)), Product, NoChange(scala212), Transformed(Seq(scala213))),
    (
      Transformed(Seq(scala213)),
      Sum,
      Transformed(Seq(scala212)),
      Transformed(Seq(scala213, scala212))
    ),
    (Transformed(Seq(scala213)), Product, Transformed(Seq(scala212)), Transformed(Seq.empty)),
    (Removed, Sum, Removed, Removed),
    (Removed, Product, Removed, Removed),
    (Removed, Sum, NoChange(scala212), Removed),
    (Removed, Sum, Transformed(Seq(scala213)), Transformed(Seq(scala213)))
  )

  test("Check Result Sum and Product") {
    forAll(fractions) { (left, operand, right, result) =>
      operand match {
        case Sum =>
          assert((left || right) == result)
        case Product =>
          assert((left && right) == result)
          assert((right && left) == result)
      }
    }
  }
}
