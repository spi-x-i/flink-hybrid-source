package org.spixi.hybrid.jobs

import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.producer.ProducerConfig
import org.slf4j.Logger
import org.spixi.hybrid.SongPlayed
import org.spixi.hybrid.configuration.Config
import org.spixi.hybrid.serde.KafkaSerializer
import org.spixi.hybrid.sources.SongPlayedGenerator

import java.time.Clock
import java.util.{Properties, UUID}

object SongPlayedGeneratorJob {

  def apply(env: StreamExecutionEnvironment, config: Config)(implicit log: Logger, clock: Clock): Unit = {
    val kafkaConfig = config.kafkaConfig
    val maxSongs    = config.songs
    val properties  = new Properties()
    properties.put(ProducerConfig.CLIENT_ID_CONFIG, "Generator Sink")

    val stream: DataStream[SongPlayed] =
      env.addSource(new SongPlayedGenerator(maxSongs)).name("Song played source").uid(UUID.randomUUID().toString)

    val kafkaSink: KafkaSink[SongPlayed] = KafkaSink
      .builder[SongPlayed]()
      .setBootstrapServers(kafkaConfig.bootstrapServers)
      .setRecordSerializer(new KafkaSerializer(kafkaConfig.registryUrl, kafkaConfig.generatorTopic))
      .setKafkaProducerConfig(properties)
      .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .build()

    stream.sinkTo(kafkaSink).name("Song played sink").uid(UUID.randomUUID().toString)
  }
}
