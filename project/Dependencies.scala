import sbt._

object Dependencies {

  // TODO: Remove on newer sbt version (https://github.com/sbt/sbt/issues/3618)
  sys.props += "packaging.type" -> "jar"

  private object flink {
    lazy val namespace = "org.apache.flink"
    lazy val version   = "1.14.4"

    lazy val scala          = namespace %% "flink-scala"                % version
    lazy val streamingScala = namespace %% "flink-streaming-scala"      % version
    lazy val stateBackends  = namespace %% "flink-statebackend-rocksdb" % version
  }

  private object slf4j {
    lazy val namespace = "org.slf4j"
    lazy val version   = "1.6.1"
    lazy val log4j     = namespace % "slf4j-log4j12" % version
  }

  private object scalatest {
    lazy val version   = "3.0.0"
    lazy val namespace = "org.scalatest"
    lazy val core      = namespace %% "scalatest" % version
  }

  lazy val flinkDependencies = Seq(
    flink.scala,
    flink.streamingScala,
    flink.stateBackends % "provided",
    slf4j.log4j,
    // test libs
    scalatest.core % Test
  )

}
