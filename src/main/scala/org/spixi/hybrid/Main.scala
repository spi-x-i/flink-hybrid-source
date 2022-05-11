package org.spixi.hybrid

import org.apache.flink.streaming.api.scala._
import org.slf4j.{Logger, LoggerFactory}
import org.spixi.hybrid.configuration.{Config, RunModes}
import org.spixi.hybrid.jobs.{SongPlayedGeneratorJob, SongToMinioJob}
import java.time.Clock

object Main extends App {
  implicit val log: Logger  = LoggerFactory.getLogger(Main.getClass)
  implicit val clock: Clock = Clock.systemUTC()

  val config = Config.load()

  config.mode match {
    case RunModes.Generator =>
      log.info(s"Chosen ${config.mode} mode. The goal is feeding data to topic ${config.kafkaConfig.generatorTopic}")
      val environment = StreamExecutionEnvironment.getExecutionEnvironment
      SongPlayedGeneratorJob(environment, config)
      environment.execute("Song Played Generator")
    case RunModes.ToMinIO =>
      log.info(s"Chosen ${config.mode} mode. The goal is storing data to minio instance")
      val environment = StreamExecutionEnvironment.getExecutionEnvironment
      SongToMinioJob(environment, config)
      environment.execute("Songs to MinIO")
    case _ =>
      log.error(
        s"Unknown run mode method. Can't proceed. exiting",
        new IllegalArgumentException(s"Unknown run mode method ${config.mode}. Can't proceed. exiting")
      )
      System.exit(-1)
  }
}
