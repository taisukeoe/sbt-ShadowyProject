# sbt-ShadowyProject

`sbt-ShadowyProject` is a sbt-plugin to define multiple sbt sub-projects which share sources, resources and jars. 

You can copy(`shadow`) your sub-project(`shadowee`) into a different one(`shadower`), with modifying original settings.

This aims to split mandatory-for-build sbt settings and useful-for-maintenance ones which may have compilation overheads into each sbt sub-project.

## How to use:

Since `sbt-ShadowyProject` is not published yet, please `git clone`, `sbt publishLocal` and add following settings. 

```
// project/plugins.sbt
addSbtPlugin("com.taisukeoe" % "sbt-shadowyproject" % "0.1-SNAPSHOT")
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
