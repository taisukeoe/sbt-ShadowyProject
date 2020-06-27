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

import sbt._

abstract class CrossType {

  def projectDir(crossBase: File, platform: Platform): File

  /** The base directory for a (true sbt) Project
    *  @param crossBase The base directory of the CrossProject
    *  @param platform JSPlatform, JVMPlatform, NativePlatform, ...
    */
  final def platformDir(crossBase: File, platform: Platform): File =
    projectDir(crossBase, platform)

  /** The location of a shared source directory (if it exists)
    *  @param projectBase the base directory of a (true sbt) Project
    *  @param conf name of sub-directory for the configuration (typically "main"
    *      or "test")
    */
  def sharedSrcDir(projectBase: File, conf: String): Option[File]

}

object CrossType {

  /** * <pre>
    * .
    * ├── js
    * ├── jvm
    * ├── native
    * └── shared
    * </pre>
    */
  object Full extends CrossType {

    def projectDir(crossBase: File, platform: Platform): File =
      crossBase / platform.identifier

    def sharedSrcDir(projectBase: File, conf: String): Option[File] =
      Some(projectBase.getParentFile / "shared" / "src" / conf / "scala")
  }

  /**
    * <pre>
    * .
    * ├── .js
    * ├── .jvm
    * ├── .native
    * └── src
    * </pre>
    */
  object Pure extends CrossType {

    def projectDir(crossBase: File, platform: Platform): File =
      crossBase / ("." + platform.identifier)

    def sharedSrcDir(projectBase: File, conf: String): Option[File] =
      Some(projectBase.getParentFile / "src" / conf / "scala")
  }

  /**
    * <pre>
    * .
    * ├── js
    * ├── jvm
    * └── native
    * </pre>
    */
  object Dummy extends CrossType {

    def projectDir(crossBase: File, platform: Platform): File =
      crossBase / platform.identifier

    def sharedSrcDir(projectBase: File, conf: String): Option[File] = None
  }
}
