[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/taisukeoe/sbt-ShadowyProject)

# sbt-ShadowyProject

`sbt-ShadowyProject` is a sbt-plugin to define an additional project which refers to another (original) project sources, resources and jars. 

You can copy(`shadow`) your original sub-project(`shadowee`) into a different one(`shadower`), with modifying original settings.

## Background
This aims to split sbt settings for **slightly** different purposes, even for the same sources.
Combination is up to you - mandatory-for-build and useful-for-maintainance, safer-coding ando flexible(,but not so safe)-coding, or strict-scalacOptions and loose-scalacOptions-with-Scalafix.
 
While there are tons of scalac-options which allow us to pursue safer coding, some of them might be too strict.

For example, `-Xfatal-warnings` (or `-Werror` in Scala 2.13 or above) scalac option is widely used to escalate warnings to errors.

It works great in situations when you want to confirm if current code is clean, such as CI. 

On the other hand, marking every unused import as an error every time seems to be a bit strict. 

Refactoring often introduces unused imports or variables, which don't have to be fixed immediately. Rather, it would be much easier to run Scalafix RemoveUnused rule, just before you commit your changes.

If that's a case, you can set `-Xfatal-warnings` to your original sub-project(shadowee), and set Scalafix configuration without `-Xfatal-warnings` to your shadower project. 

## How to use: 

```
// project/plugins.sbt
addSbtPlugin("com.taisukeoe" % "sbt-shadowyproject" % "0.1.0-RC2")
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

## Shadow or Shade?

There are two kinds of ShadowyProject: Shadow and Shade. Shade is a shorthand version of Shadow.  

Shadow will use the same source, resources, jars *AND* other settings as the original shadowee project.

You can modify these copied settings with *modification algebras*, described below. 

Shade will use the same source, resources and jars as the original shadowee project.

You can manually add *common* settings into Shade project as you do in typical sbt subprojects.

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
