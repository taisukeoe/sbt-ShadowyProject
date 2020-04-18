package dev.taisukeoe

import sbt._

trait ScopeSelectable[Axis] {
  def asScope(axis: Axis): Scope
}

object ScopeSelectable {
  implicit val scope: ScopeSelectable[Scope] = new ScopeSelectable[Scope] {
    override def asScope(scope: Scope) = scope
  }

  implicit val configScope: ScopeSelectable[Configuration] =
    new ScopeSelectable[Configuration] {
      override def asScope(config: Configuration) =
        Scope(This, Select(config), This, This)
    }

  implicit val projectScope: ScopeSelectable[Project] =
    new ScopeSelectable[Project] {
      override def asScope(project: Project): Scope =
        Scope(Select(project), This, This, This)
    }

  implicit val taskScope: ScopeSelectable[AttributeKey[_]] =
    new ScopeSelectable[AttributeKey[_]] {
      override def asScope(task: AttributeKey[_]): Scope =
        Scope(This, This, Select(task), This)
    }

  object Ops {
    implicit class ScopeSelectableOps[Axis](axis: Axis)(
        implicit E: ScopeSelectable[Axis]
    ) {
      def asScope: Scope = E.asScope(axis)
    }
  }
}
