package com.taisukeoe.composite

/*
Copied and pasted from sbt-crossproject.

https://github.com/portable-scala/sbt-crossproject

----
Copyright (c) 2016, Denys Shabalin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

 * Neither the name of sbt-crossproject-project nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.File
import scala.reflect.macros.Context

private[composite] object CrossProjectMacros {
  def crossProject_impl(c: Context)(platformsArgs: List[c.Expr[Platform]]): c.Expr[CrossProject.Builder] = {
    import c.universe._

    val enclosingValName = MacroUtils.definingValName(
      c,
      methodName => s"""$methodName must be directly assigned to a val, such as `val x = $methodName`."""
    )

    val name = Literal(Constant(enclosingValName))

    def javaIoFile =
      reify { new _root_.java.io.File(c.Expr[String](name).splice) }.tree

    val platforms = platformsArgs.map(_.tree).toList

    val crossProjectCompanionTerm =
      Select(
        Select(
          Select(
            Select(
              Ident(newTermName("_root_")),
              newTermName("com")
            ),
            newTermName("taisukeoe")
          ),
          newTermName("composite")
        ),
        newTermName("CrossProject")
      )

    val applyFun =
      Select(
        crossProjectCompanionTerm,
        newTermName("apply")
      )

    c.Expr[CrossProject.Builder](
      Apply(
        Apply(
          applyFun,
          List(name, javaIoFile)
        ),
        platforms
      )
    )
  }

  def vargCrossProject_impl(c: Context)(platforms: c.Expr[Platform]*): c.Expr[CrossProject.Builder] = {
    import c.universe._
    val withDefaults: List[c.Expr[Platform]] = reify(JVMPlatform) :: reify(Refactoring) :: platforms.toList
    crossProject_impl(c)(withDefaults)
  }
}
