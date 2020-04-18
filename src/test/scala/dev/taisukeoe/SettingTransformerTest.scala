package dev.taisukeoe

import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.prop.TableDrivenPropertyChecks

class SettingTransformerTest extends AsyncFunSuite with TableDrivenPropertyChecks with SettingTransformerTestBase {
  import SettingTransformer._

  private val fractions = Table[Result, Operand, Result, Result](
    ("left", "operand", "right", "Result"),
    (originalAlg, Sum, Removed, Removed),
    (originalAlg, Product, Removed, Removed),
    (originalAlg, Sum, removeWerrorAlg, removeWerrorAlg),
    (originalAlg, Product, removeWerrorAlg, removeWerrorAlg),
    (originalAlg, Sum, originalAlg, originalAlg),
    (originalAlg, Product, originalAlg, originalAlg),
    (removeWerrorAlg, Sum, Removed, removeWerrorAlg),
    (removeWerrorAlg, Product, Removed, Removed),
    (removeWerrorAlg, Sum, originalAlg, removeWerrorAlg),
    (removeWerrorAlg, Product, originalAlg, removeWerrorAlg),
    (removeWerrorAlg, Sum, addDeprecationAlg, Add(original, Seq(removeWerror, addDeprecation))),
    (removeWerrorAlg, Product, addDeprecationAlg, Add(original, Seq(removeWerror, addDeprecation))),
    (Removed, Sum, Removed, Removed),
    (Removed, Product, Removed, Removed),
    (Removed, Sum, originalAlg, Removed),
    (Removed, Product, originalAlg, Removed),
    (Removed, Sum, removeWerrorAlg, removeWerrorAlg),
    (Removed, Product, removeWerrorAlg, Removed)
  )

  test("Check Result Sum and Product") {
    forAll(fractions) { (left, operand, right, result) =>
      operand match {
        case Sum =>
          assert((left || right) == result)
        case Product =>
          assert((left && right) == result)
      }
    }
  }
}
