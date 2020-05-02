package dev.taisukeoe

import sbt.Keys.scalacOptions
import sbt.Keys.target
import sbt._

sealed trait SettingTransformer {

  def transform(setting: Setting[_]): SettingTransformer.Result

  def +(that: SettingTransformer): SettingTransformer = SettingTransformer.Plus(this, that)
}

object SettingTransformer {
  sealed trait Result {
    def +(that: Result): Result
    def newSettings: Seq[Setting[_]]
  }
  object Result {
    def unless(cond: Boolean)(setting: => Setting[_]): Result =
      if (cond) Removed else NoChange(setting)
  }

  //Just remove a shadowee key and value.
  case object Removed extends Result {

    override def +(that: Result): Result = this

    override def newSettings: Seq[Setting[_]] = Nil
  }

  //Keep a shadowee key and value
  final case class NoChange(setting: Setting[_]) extends Result {

    override def +(that: Result): Result = that

    override def newSettings: Seq[Setting[_]] = Seq(setting)
  }

  /*
   * Besides of a shadowee original key and value, this adds the same key with a modified value.
   * `added` argument must be a sequential, not a set, since an order in Seq[Setting[_]] matter in sbt.
   *
   * In case you want to modify a shadowee setting,
   *   1.) Just use Removed Result for the shadowee setting and add a new shadower setting separately.
   *   2.) Use Add Result with `~=` setting operator to modify the original setting.
   *
   * NOTE: No algebra to just change a value in a shadowee setting is prepared,
   * since its proper `+` operator cannot be defined.
   */
  final case class Add(original: Setting[_], added: Seq[Setting[_]]) extends Result {
    override def +(that: Result): Result =
      that match {
        case Add(thatOriginal, thatAdded) =>
          require(
            original == thatOriginal,
            s"Both of Add algebras must have the same original, but $original and $thatOriginal"
          )
          Add(original, added ++ thatAdded)
        case NoChange(_) => this
        case Removed => that
      }

    override def newSettings: Seq[Setting[_]] = original +: added
  }

  final case class Plus(left: SettingTransformer, right: SettingTransformer)
      extends SettingTransformer {
    override def transform(setting: Setting[_]): Result =
      left.transform(setting) + right.transform(setting)
  }

  //   Its scope will be kept in Configuration axis and in Task axis.
  //   Project axis will be overwritten by the shadower project.
  final case class Modify(private val mod: Setting[_] => Result) extends SettingTransformer {
    override def transform(setting: Setting[_]): Result = mod(setting)
  }

  val RemoveXFatalWarnings: SettingTransformer = RemoveScalacOptions("-Xfatal-warnings", "-Werror")

  def RemoveScalacOptions(names: String*): SettingTransformer = {
    Modify {
      case setting if setting.key.key.label == scalacOptions.key.label =>
        Add(setting, Seq(scalacOptions in setting.key.scope --= names))
      case s => NoChange(s)
    }
  }

  def ShadowScopedSettingKey[T](shadowee: Project, targetKey: SettingKey[T]): SettingTransformer =
    Modify {
      case setting if setting.key.key.label == targetKey.key.label =>
        Add(
          setting,
          Seq(targetKey.in(setting.key.scope) := targetKey.in(setting.key.scope).in(shadowee).value)
        )
      case s => NoChange(s)
    }

  def ShadowScopedTaskKey[T](shadowee: Project, targetKey: TaskKey[T]): SettingTransformer =
    Modify {
      case setting if setting.key.key.label == targetKey.key.label =>
        Add(
          setting,
          Seq(targetKey.in(setting.key.scope) := targetKey.in(setting.key.scope).in(shadowee).value)
        )
      case s => NoChange(s)
    }

  def ShadowScopedInputKey[T](shadowee: Project, targetKey: InputKey[T]): SettingTransformer =
    Modify {
      case setting if setting.key.key.label == targetKey.key.label =>
        Add(
          setting,
          Seq(
            targetKey
              .in(setting.key.scope) := targetKey.in(setting.key.scope).in(shadowee).evaluated
          )
        )
      case s => NoChange(s)
    }

  val RemoveTargetDir: SettingTransformer = ExcludeKeyNames(Set(target.key.label))

  final case class ExcludeKeyNames(names: Set[String]) extends SettingTransformer {
    override def transform(setting: Setting[_]): Result =
      Result.unless(names(setting.key.key.label))(setting)
  }

  final case class ExcludeConfigScoped(configs: Set[ConfigKey]) extends SettingTransformer {
    override def transform(setting: Setting[_]): Result =
      setting.key match {
        case Def.ScopedKey(Scope(_, Select(conf), _, _), _) if configs(conf) => Removed
        case _ => NoChange(setting)
      }
  }

  final case class ExcludeTaskScoped(keys: Set[AttributeKey[_]]) extends SettingTransformer {
    override def transform(setting: Setting[_]): Result =
      setting.key match {
        case Def.ScopedKey(Scope(_, _, Select(key), _), _) if keys(key) => Removed
        case _ => NoChange(setting)
      }
  }

  final case class ExcludeProjectScoped(references: Set[Reference]) extends SettingTransformer {
    override def transform(setting: Setting[_]): Result =
      setting.key match {
        case Def.ScopedKey(Scope(Select(refs), _, _, _), _) if references(refs) => Removed
        case _ => NoChange(setting)
      }
  }

  case object Empty extends SettingTransformer {
    override def transform(setting: Setting[_]): Result = NoChange(setting)
  }

  case object RemoveAll extends SettingTransformer {
    override def transform(setting: Setting[_]): Result = Removed
  }
}
