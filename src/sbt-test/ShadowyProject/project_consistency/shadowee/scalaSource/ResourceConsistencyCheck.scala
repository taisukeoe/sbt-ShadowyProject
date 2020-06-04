object ResourceConsistencyCheck {
  def main(args: Array[String]): Unit = {
    Seq(
      "resourceGenerators/resourceGenerators.conf",
      "managedResources.conf",
      "resourceDirectory.conf",
      "unmanagedResources.conf",
      "unmanagedResourceDirectories.conf"
    ).foreach(conf =>
      assert(null != getClass.getResource(conf), s"getResource($conf) must not return null.")
    )
  }
}
