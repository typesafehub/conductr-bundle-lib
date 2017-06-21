import Tests._

name := "akka23-conductr-bundle-lib"

libraryDependencies ++= List(
  // Adding akka23Http here is necessary because akka23-common is adding the library as 'provided'
  Library.akka23Http    % "provided",
  Library.akka23Cluster % "provided",
  Library.akka23Testkit % "test",
  Library.scalaTest     % "test"
)

crossScalaVersions := crossScalaVersions.value.filterNot(_.startsWith("2.12"))

fork in Test := true

def groupByFirst(tests: Seq[TestDefinition]): Seq[Group] =
  tests
    .groupBy(t => t.name.drop(t.name.indexOf("WithEnv")))
    .map {
      case ("WithEnvForHost", t) =>
        new Group("WithEnvForHost", t, SubProcess(ForkOptions(envVars = Map(
          "BUNDLE_HOST_IP" -> "10.0.1.10",
          "BUNDLE_SYSTEM" -> "some-system",
          "BUNDLE_SYSTEM_VERSION" -> "v1",
          "AKKA_REMOTE_PROTOCOL" -> "tcp",
          "AKKA_REMOTE_HOST_PORT" -> "10000",
          "AKKA_REMOTE_OTHER_PROTOCOLS" -> "",
          "AKKA_REMOTE_OTHER_IPS" -> "",
          "AKKA_REMOTE_OTHER_PORTS" -> ""))))
      case ("WithEnvForOneOther", t) =>
        new Group("WithEnvForOneOther", t, SubProcess(ForkOptions(envVars = Map(
          "BUNDLE_HOST_IP" -> "10.0.1.10",
          "BUNDLE_SYSTEM" -> "some-system",
          "BUNDLE_SYSTEM_VERSION" -> "v1",
          "AKKA_REMOTE_PROTOCOL" -> "tcp",
          "AKKA_REMOTE_HOST_PORT" -> "10000",
          "AKKA_REMOTE_OTHER_PROTOCOLS" -> "udp",
          "AKKA_REMOTE_OTHER_IPS" -> "10.0.1.11",
          "AKKA_REMOTE_OTHER_PORTS" -> "10001"))))
      case ("WithEnvForOthers", t) =>
        new Group("WithEnvForOthers", t, SubProcess(ForkOptions(envVars = Map(
          "BUNDLE_HOST_IP" -> "10.0.1.10",
          "BUNDLE_SYSTEM" -> "some-system",
          "BUNDLE_SYSTEM_VERSION" -> "v1",
          "AKKA_REMOTE_PROTOCOL" -> "tcp",
          "AKKA_REMOTE_HOST_PORT" -> "10000",
          "AKKA_REMOTE_OTHER_PROTOCOLS" -> "udp:tcp",
          "AKKA_REMOTE_OTHER_IPS" -> "10.0.1.11:10.0.1.12",
          "AKKA_REMOTE_OTHER_PORTS" -> "10001:10000"))))
      case ("WithEnv", t) =>
        new Group("WithEnv", t, SubProcess(ForkOptions(envVars = Map(
          "BUNDLE_ID" -> "0BADF00DDEADBEEF",
          "CONDUCTR_STATUS" -> "http://127.0.0.1:60007",
          "SERVICE_LOCATOR" -> "http://127.0.0.1:60008/services"))))
      case (x, t) =>
        new Group("WithoutEnv", t, SubProcess(ForkOptions()))
    }.toSeq

testGrouping in Test := { (definedTests in Test).map(groupByFirst).value }
