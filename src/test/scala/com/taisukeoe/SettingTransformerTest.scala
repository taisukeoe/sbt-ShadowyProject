package com.taisukeoe

import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.prop.TableDrivenPropertyChecks

class SettingTransformerTest
    extends AsyncFunSuite
    with TableDrivenPropertyChecks
    with SettingTransformerTestBase {
  import SettingTransformer._

  private val fractions = Table[Action, Action, Action](
    ("left", "right", "Result"),
    (originalAlg, Remove, Remove),
    (originalAlg, removeWerrorAlg, removeWerrorAlg),
    (originalAlg, originalAlg, originalAlg),
    (removeWerrorAlg, Remove, Remove),
    (removeWerrorAlg, originalAlg, removeWerrorAlg),
    (removeWerrorAlg, addDeprecationAlg, Add(original, Seq(removeWerror, addDeprecation))),
    (Remove, Remove, Remove),
    (Remove, originalAlg, Remove),
    (Remove, removeWerrorAlg, Remove)
  )

  test("Result algebras") {
    forAll(fractions) { (left, right, result) =>
      assert(left + right == result)
    }
  }
}
