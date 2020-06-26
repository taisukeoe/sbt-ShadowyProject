trait JarConsistencyCheck {
  /*
   * Jars which contain the interfaces have been parepared by:
   *  sh setup_jars.sh
   */
  def ub: UnmanagedBase
  def uj: UnmanagedJars
  def uc: UnmanagedClasspath
  def mc: ManagedClasspath
  def idc: InternalDependencyClasspath
  def edc: ExternalDependencyClasspath
  def dc: DependencyClasspath
}
