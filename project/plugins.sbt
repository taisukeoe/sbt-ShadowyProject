libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.2")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.16")

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile / "src" / "main" / "scala"
