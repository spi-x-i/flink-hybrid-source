import sbt._

object Dependencies {

  // TODO: Remove on newer sbt version (https://github.com/sbt/sbt/issues/3618)
  sys.props += "packaging.type" -> "jar"

  private object flink {
    lazy val namespace = "org.apache.flink"
    lazy val version   = "1.14.4"

    lazy val scala          = namespace %% "flink-scala"                % version
    lazy val streamingScala = namespace %% "flink-streaming-scala"      % version
    lazy val clients        = namespace %% "flink-clients"              % version
    lazy val avro           = namespace  % "flink-avro"                 % version
    lazy val stateBackends  = namespace %% "flink-statebackend-rocksdb" % version
    lazy val kafka          = namespace %% "flink-connector-kafka"      % version
    lazy val files          = namespace  % "flink-connector-files"      % version
  }

  private object slf4j {
    lazy val namespace = "org.slf4j"
    lazy val version   = "1.7.30"
    lazy val log4j     = namespace % "log4j-over-slf4j" % version
  }

  private object logback {
    lazy val namespace = "ch.qos.logback"
    lazy val version   = "1.2.3"
    lazy val classic   = namespace % "logback-classic" % version
  }

  private object pureConfig {
    lazy val version   = "0.16.0"
    lazy val namespace = "com.github.pureconfig"
    lazy val core      = namespace %% "pureconfig" % version
  }

  private object kafkaAvroSerde {
    lazy val version   = "5.5.1"
    lazy val namespace = "io.confluent"
    lazy val core      = namespace % "kafka-avro-serializer" % version
  }

  private object kafka {
    lazy val namespace = "org.apache.kafka"
    lazy val version   = "2.5.1"
    lazy val core      = namespace % "kafka-clients" % version
  }

  private object avro {
    lazy val version   = "1.9.2"
    lazy val namespace = "org.apache.avro"
    lazy val core      = namespace % "avro" % version
  }

  private object scalatest {
    lazy val version   = "3.0.0"
    lazy val namespace = "org.scalatest"
    lazy val core      = namespace %% "scalatest" % version
  }

  lazy val flinkDependencies = Seq(
    flink.scala,
    flink.streamingScala,
    flink.files,
    flink.kafka,
    flink.avro,
    flink.stateBackends % "provided",
    flink.clients, // must be provided
    slf4j.log4j,
    logback.classic,
    pureConfig.core,
    avro.core,
    kafka.core,
    kafkaAvroSerde.core,
    // test libs
    scalatest.core % Test
  )

}
