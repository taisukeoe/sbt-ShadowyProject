package dev.taisukeoe

import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.prop.TableDrivenPropertyChecks

class SettingTransformerTest extends AsyncFunSuite with TableDrivenPropertyChecks with SettingTransformerTestBase {
  import SettingTransformer._

  private val fractions = Table[Result, Result, Result](
    ("left", "right", "Result"),
    (originalAlg, Removed, Removed),
    (originalAlg, removeWerrorAlg, removeWerrorAlg),
    (originalAlg, originalAlg, originalAlg),
    (removeWerrorAlg, Removed, Removed),
    (removeWerrorAlg, originalAlg, removeWerrorAlg),
    (removeWerrorAlg, addDeprecationAlg, Add(original, Seq(removeWerror, addDeprecation))),
    (Removed, Removed, Removed),
    (Removed, originalAlg, Removed),
    (Removed, removeWerrorAlg, Removed)
  )

  test("Check Result Sum and Product") {
    forAll(fractions) { (left, right, result) =>
      assert(left + right == result)
    }
  }
}
