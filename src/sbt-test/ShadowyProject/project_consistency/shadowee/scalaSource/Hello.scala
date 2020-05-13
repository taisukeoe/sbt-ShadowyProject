object Hello {
  /*
   * Following sbt Keys are NOT used by sbt.Keys.sources, so that they are eventually ignored.
   *  val msd: ManagedSourceDirectories = ???
   *  val sd: SourceDirectories = ???
   *  val sm: SourceManaged = ???
   */
  val ms: ManagedSources = ???
  val s: Sources = ???
  val sg: SourceGenerators = ???
  val us: UnmanagedSources = ???
  val usd: UnmanagedSourceDirectories = ???
}
