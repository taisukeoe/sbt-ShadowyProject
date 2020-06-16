package com.taisukeoe

import com.taisukeoe.SettingTransformer.Action
import com.taisukeoe.SettingTransformer.Remove
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Properties

class SettingTransformerLaws extends Properties("SettingTransformer laws") with SettingTransformerTestBase {

  private val gen = Gen.oneOf[Action](Seq(originalAlg, Remove, removeWerrorAlg, addDeprecationAlg))

  property("associativity") = Prop.forAll(gen, gen, gen) {
    case (left, mid, right) =>
      ((left + mid) + right) == (left + (mid + right))
  }
}
