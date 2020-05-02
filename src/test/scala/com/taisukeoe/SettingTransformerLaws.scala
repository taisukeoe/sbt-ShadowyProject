package com.taisukeoe

import com.taisukeoe.SettingTransformer.Removed
import com.taisukeoe.SettingTransformer.Result
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Properties

class SettingTransformerLaws
    extends Properties("SettingTransformer laws")
    with SettingTransformerTestBase {

  private val gen = Gen.oneOf[Result](Seq(originalAlg, Removed, removeWerrorAlg, addDeprecationAlg))

  property("associativity") = Prop.forAll(gen, gen, gen) {
    case (left, mid, right) =>
      ((left + mid) + right) == (left + (mid + right))
  }
}
