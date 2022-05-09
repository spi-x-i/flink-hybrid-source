package org.spixi.hybrid.configuration

import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import pureconfig.generic.semiauto.deriveEnumerationReader
import pureconfig.{ConfigFieldMapping, ConfigReader, ConfigSource}

object RunModes {
  sealed trait RunMode

  case object Generator extends RunMode
  case object ToMinIO   extends RunMode
  case object Count     extends RunMode
}

final case class Config(songs: Int, mode: RunModes.RunMode, kafkaConfig: KafkaConfig, s3Config: S3Config)
final case class KafkaConfig(bootstrapServers: String, generatorTopic: String, registryUrl: String)
final case class S3Config(outputPath: String, url: String)

object Config {
  implicit val modeConverter: ConfigReader[RunModes.RunMode] = deriveEnumerationReader[RunModes.RunMode]

  def load(): Config = configResolver(ConfigSource.default.load[Config])

  private def configResolver: ConfigReader.Result[Config] => Config = {
    case Right(config) => config
    case Left(error)   => throw new RuntimeException(s"Cannot read config file, errors:\n ${error.toList.mkString("\n")}")
  }
}
