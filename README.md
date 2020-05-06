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

NOTE: sbt-ShadowyProject `build.sbt` file is a good sample about how-to-use itself.  

## Modification Algebras

`SettingTransformer` traits define the ways to modify original settings.

There are pre-defined transformers like:

- `RemoveScalacOptions`
  - `RemoveXFatalWarnings`
- `ExcludeKeyNames`
  - `RemoveTargetDir` (applied to all shadower projects at default)
- `ExcludeConfigScoped`
  
`SettingsTransformer` and its `Result` Algebras have a plus(`+`) operator and satisfy an associative law.
