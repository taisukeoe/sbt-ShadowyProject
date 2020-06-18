[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/taisukeoe/sbt-ShadowyProject)

# sbt-ShadowyProject

`sbt-ShadowyProject` is a sbt-plugin to define an additional project which refers to another (original) project sources, resources and jars. 

You can copy(`shadow`) your original sub-project(`shadowee`) into a different one(`shadower`), with modifying original settings.

## How to use: 

```
// project/plugins.sbt
addSbtPlugin("com.taisukeoe" % "sbt-shadowyproject" % "0.1.0")
```

```
// build.sbt

lazy val yourMainProject = (project in file("core"))
  .settings(/*...*/)

lazy val shadow = project
  .shadow(yourMainProject) // enriched by sbt-shadowyproject
  .modify(/* define how to modify original settings with modification algebras */)
  .settings(/* add shadowee specific settings */)
  .light // applied all modification and settings to `shadow` sub-project
```

In case you needn't copy nor *modify* original shadowee settings, you can call `project.shade(yourMainProject).light` instead.

NOTE: sbt-ShadowyProject `build.sbt` file is a good real world example.

## Background
This aims to split sbt settings for **slightly** different purposes, even for the same sources.
Combination is up to you - mandatory-for-build and useful-for-maintainance, safer-coding ando flexible(,but not so safe)-coding, or strict-scalacOptions and loose-scalacOptions-with-Scalafix.
 
While there are tons of scalac-options which allow us to pursue safer coding, some of them might be too strict.

For example, `-Xfatal-warnings` (or `-Werror` in Scala 2.13 or above) scalac option is widely used to escalate warnings to errors.

It works great in situations when you want to confirm if current code is clean, such as CI. 

On the other hand, marking every unused import as an error every time seems to be a bit strict. 

Refactoring often introduces unused imports or variables, which don't have to be fixed immediately. Rather, it would be much easier to run Scalafix RemoveUnused rule, just before you commit your changes.

If that's a case, you can set `-Xfatal-warnings` to your original sub-project(shadowee), and set Scalafix configuration without `-Xfatal-warnings` to your shadower project. 

### deepShadow

DeepShadow is an experimental feature that you want to gather all sources from your dependency.

Imagine the following build.sbt is given.

```build.sbt
lazy val a1 = project
lazy val a2 = project.dependsOn(a1 % "compile->compile;test->test")
lazy val a3 = project.dependsOn(a2)
lazy val a4 = project.dependsOn(a3 % "compile->compile;test->test")
lazy val a5 = project.dependsOn(a4 % "compile->compile;test->test")

lazy val deepShadowA5 = project
  .deepShadow(a5)
  .light
```

`sources` files in compile or test scope are as follows.

```
sbt> show deepShadowA5/compile:sources

[info] [info] * /private/var/folders/64/dlcmkyhd7c58mknm5_66xnjc0000gn/T/sbt_e143c3c7/project_dependency/a5/src/main/scala/A5.scala
[info] [info] * /private/var/folders/64/dlcmkyhd7c58mknm5_66xnjc0000gn/T/sbt_e143c3c7/project_dependency/a4/src/main/scala/A4.scala
[info] [info] * /private/var/folders/64/dlcmkyhd7c58mknm5_66xnjc0000gn/T/sbt_e143c3c7/project_dependency/a3/src/main/scala/A3.scala
[info] [info] * /private/var/folders/64/dlcmkyhd7c58mknm5_66xnjc0000gn/T/sbt_e143c3c7/project_dependency/a2/src/main/scala/A2.scala
[info] [info] * /private/var/folders/64/dlcmkyhd7c58mknm5_66xnjc0000gn/T/sbt_e143c3c7/project_dependency/a1/src/main/scala/A1.scala
```

```
sbt> show deepShadowA5/test:sources

[info] [info] * /private/var/folders/64/dlcmkyhd7c58mknm5_66xnjc0000gn/T/sbt_e143c3c7/project_dependency/a5/src/test/scala/A5Test.scala
[info] [info] * /private/var/folders/64/dlcmkyhd7c58mknm5_66xnjc0000gn/T/sbt_e143c3c7/project_dependency/a4/src/test/scala/A4Test.scala
[info] [info] * /private/var/folders/64/dlcmkyhd7c58mknm5_66xnjc0000gn/T/sbt_e143c3c7/project_dependency/a3/src/test/scala/A3Test.scala
```

So that you can run a task with your favorite config (e.g. compile with your favorite scalacOptions or run Scalafix with preferable settings) against all sub-project sources at once.

## Assumptions
ShadowyProject assumes original project settings:

- don't have files to be added to sources or resources TaskKeys directly.
- don't have files for its generators, managed sources or resources which are not located under sourceManaged or resourceManaged directories. 

Otherwise, ShadowyProject cannot refer them automatically and requires to add them to ShadowyProject settings explicitly.

## Modification Algebras

`SettingTransformer` traits define the ways to modify original settings.

Shadow has a public `modify` method to add a SettingTransformer, while Shade has only a private constructor parameter for it. 

There are pre-defined transformers like:

- `RemoveScalacOptions`
  - `RemoveXFatalWarnings`
- `ExcludeKeyNames`
  - `RemoveTargetDir` (applied to all Shadow projects at default)
- `ExcludeConfigScoped`
  
`SettingsTransformer` and `Action` Algebras have a plus(`+`) operator and satisfy an associative law.
