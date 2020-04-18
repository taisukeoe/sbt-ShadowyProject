package dev.taisukeoe

import dev.taisukeoe.SettingTransformer.{Removed, Result}
import org.scalacheck.{Gen, Prop, Properties}

class SettingTransformerLaws
    extends Properties("SettingTransformer laws")
    with SettingTransformerTestBase {

  private val gen = Gen.oneOf[Result](Seq(originalAlg, Removed, removeWerrorAlg, addDeprecationAlg))

  property("associativity") = Prop.forAll(gen, gen, gen) {
    case (left, mid, right) =>
      ((left + mid) + right) == (left + (mid + right))
  }
}
