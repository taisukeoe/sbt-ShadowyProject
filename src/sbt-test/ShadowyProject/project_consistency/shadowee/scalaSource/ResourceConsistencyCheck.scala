object ResourceConsistencyCheck {
  def main(args: Array[String]): Unit = {
    Seq("resourceGenerators.conf",
      "managedResources.conf",
      "resources.conf",
      "resourceDirectory.conf",
      "unmanagedResources.conf",
      "unmanagedResourceDirectories.conf"
    ).foreach(conf =>
      assert(null != getClass.getResource(conf), s"getResource($conf) must not return null.")
    )
  }
}
