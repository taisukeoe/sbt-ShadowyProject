trait SourceConsistencyCheck {
  def js: JavaSource
  def ms: ManagedSources
  def s: Sources
  def sg: SourceGenerators
  def us: UnmanagedSources
  def usd: UnmanagedSourceDirectories
}
