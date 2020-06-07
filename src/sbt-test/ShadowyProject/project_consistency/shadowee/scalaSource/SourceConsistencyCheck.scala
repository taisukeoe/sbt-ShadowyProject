trait SourceConsistencyCheck {
  def js: JavaSource
  def ms: ManagedSources
  def sg: SourceGenerators
  def us: UnmanagedSources
  def usd: UnmanagedSourceDirectories
}
