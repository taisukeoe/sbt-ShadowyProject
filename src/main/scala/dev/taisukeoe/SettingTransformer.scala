package dev.taisukeoe

import sbt._
import sbt.Keys.{scalacOptions, target}

sealed trait SettingTransformer {

  def transform(setting: Def.Setting[_]): SettingTransformer.Result

  def &&(that: SettingTransformer): SettingTransformer =
    SettingTransformer.And(this, that)

  def ||(that: SettingTransformer): SettingTransformer =
    SettingTransformer.Or(this, that)

}

object SettingTransformer {
  sealed trait Result {
    def ||(that: Result): Result
    def &&(that: Result): Result
    def newSettings: Seq[Def.Setting[_]]
  }
  object Result {
    def unless(cond: Boolean)(setting: => Def.Setting[_]): Result =
      if (cond) Removed else NoChange(setting)
  }

  case object Removed extends Result {
    override def ||(that: Result): Result = that match {
      case NoChange(_) => this
      case _ => that
    }

    override def &&(that: Result): Result = this

    override def newSettings: Seq[Def.Setting[_]] = Nil
  }

  final case class Transformed(newSettings: Seq[Def.Setting[_]])
      extends Result {
    override def ||(that: Result): Result = that match {
      case Transformed(those) => this.copy(newSettings ++ those)
      case _ => this
    }

    override def &&(that: Result): Result = that match {
      case Transformed(those) => this.copy(newSettings.intersect(those))
      case NoChange(_) => this
      case Removed => that
    }
  }

  final case class NoChange(setting: Def.Setting[_]) extends Result {
    override def ||(that: Result): Result = that

    override def &&(that: Result): Result = that

    override def newSettings: Seq[Def.Setting[_]] = Seq(setting)
  }

  final case class And(left: SettingTransformer, right: SettingTransformer)
      extends SettingTransformer {
    override def transform(setting: Def.Setting[_]): Result =
      left.transform(setting) && right.transform(setting)
  }

  final case class Or(left: SettingTransformer, right: SettingTransformer)
      extends SettingTransformer {
    override def transform(setting: Def.Setting[_]): Result =
      left.transform(setting) || right.transform(setting)
  }

  final case class Modify(private val mod: Def.Setting[_] => Result)
      extends SettingTransformer {
    override def transform(setting: Def.Setting[_]): Result = mod(setting)
  }

  val RemoveXFatalWarnings: Modify = RemoveScalacOptions("-Xfatal-warnings")

  def RemoveScalacOptions(names: String*): Modify = Modify {
    case setting if setting.key.key.label == scalacOptions.key.label =>
      Transformed(
        Seq(setting, scalacOptions in setting.key.scope --= names)
      )
    case s => NoChange(s)
  }

  val RemoveTarget: ExcludeKeyNames = ExcludeKeyNames(Set(target.key.label))

  final case class ExcludeKeyNames(names: Set[String])
      extends SettingTransformer {
    override def transform(setting: Def.Setting[_]): Result =
      Result.unless(names(setting.key.key.label))(setting)
  }

  final case class ExcludeConfigScoped(configs: Set[ConfigKey])
      extends SettingTransformer {
    override def transform(setting: Def.Setting[_]): Result =
      setting.key match {
        case Def.ScopedKey(Scope(_, Select(conf), _, _), _) if configs(conf) =>
          Removed
        case _ => NoChange(setting)
      }
  }

  final case class ExcludeTaskScoped(keys: Set[AttributeKey[_]])
      extends SettingTransformer {
    override def transform(setting: Def.Setting[_]): Result =
      setting.key match {
        case Def.ScopedKey(Scope(_, _, Select(key), _), _) if keys(key) =>
          Removed
        case _ => NoChange(setting)
      }
  }

  final case class ExcludeProjectScoped(references: Set[Reference])
      extends SettingTransformer {
    override def transform(setting: Def.Setting[_]): Result =
      setting.key match {
        case Def.ScopedKey(Scope(Select(refs), _, _, _), _)
            if references(refs) =>
          Removed
        case _ => NoChange(setting)
      }
  }

//  final case class ExcludeScope(references: Set[Reference], keys: Set[AttributeKey[_]], configs: Set[ConfigKey]) extends SettingTransformer {
//    override def excludes(setting: Def.Setting[_]): Boolean = setting.key match {
//      case Def.ScopedKey(Scope(Select(refs), _, _, _), _) if references(refs) => true
//      case _ => false
//    }
//  }

  case object Empty extends SettingTransformer {
    override def transform(setting: Def.Setting[_]): Result = NoChange(setting)
  }
}
