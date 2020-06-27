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

import sbt._

import java.io.File

abstract class CrossType {

  /** The base directory for a (true sbt) Project
    *  @param crossBase The base directory of the CrossProject
    *  @param projectType "jvm" or "js". Other values may be supported
    */
  @deprecated("use platformDir", "0.1.0")
  def projectDir(crossBase: File, projectType: String): File

  def projectDir(crossBase: File, platform: Platform): File

  /** The base directory for the JVM project */
  @deprecated("use platformDir(crossBase, JVMPlatform)", "0.1.0")
  final def jvmDir(crossBase: File): File = platformDir(crossBase, JVMPlatform)

  /** The base directory for the JS project */
  @deprecated("use platformDir(crossBase, JSPlatform)", "0.1.0")
  final def jsDir(crossBase: File): File = projectDir(crossBase, "js")

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

    @deprecated("use projectDir(crossBase: File, platform: Platform): File", "0.1.0")
    def projectDir(crossBase: File, projectType: String): File =
      crossBase / projectType

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
    @deprecated("use projectDir(crossBase: File, platform: Platform): File", "0.1.0")
    def projectDir(crossBase: File, projectType: String): File =
      crossBase / ("." + projectType)

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
    @deprecated("use projectDir(crossBase: File, platform: Platform): File", "0.1.0")
    def projectDir(crossBase: File, projectType: String): File =
      crossBase / projectType

    def projectDir(crossBase: File, platform: Platform): File =
      crossBase / platform.identifier

    def sharedSrcDir(projectBase: File, conf: String): Option[File] = None
  }
}
