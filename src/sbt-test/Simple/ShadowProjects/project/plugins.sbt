sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("dev.taisukeoe" % "sbt-shadowprojects" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
