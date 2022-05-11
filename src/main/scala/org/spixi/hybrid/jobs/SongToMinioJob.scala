package org.spixi.hybrid.jobs

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.{ConsumerConfig, OffsetResetStrategy}
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.slf4j.Logger
import org.spixi.hybrid.configuration.Config
import org.spixi.hybrid.serde.{KafkaDeserializerCertilogo, KafkaDeserializerCertilogoGeneric}

import java.time.Clock
import java.util.Properties
import java.{lang, util}
import scala.collection.JavaConverters._
import org.apache.avro.generic.GenericRecord

object SongToMinioJob {

  def apply(env: StreamExecutionEnvironment, config: Config)(implicit log: Logger, clock: Clock): Unit = {
    val kafkaConfig = config.kafkaConfig
    val s3Config    = config.s3Config
    val sourceName  = "certilogo spixi consumer"
    val sourceTopic =
      "ING_65794a8b-510e-4c91-8e3a-36fcce64f81c_c8ad49d7-9f2e-498b-b703-b68f2c134b51_authentication_sessions"
    val properties = new Properties()
    properties.put(ProducerConfig.CLIENT_ID_CONFIG, "Generator Sink")

    val offsets: util.Map[TopicPartition, java.lang.Long] = (0 to 47)
      .map { id =>
        val key   = new TopicPartition(sourceTopic, id)
        val value = new lang.Long(194000)
        key -> value
      }
      .toMap
      .asJava

    val props = new Properties()
    props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000)

    val kafkaSource = KafkaSource
      .builder[GenericRecord]()
      .setBootstrapServers(kafkaConfig.bootstrapServers)
      .setTopics(sourceTopic)
      .setDeserializer(new KafkaDeserializerCertilogoGeneric(kafkaConfig.registryUrl, sourceTopic))
      .setGroupId(sourceName)
      .setStartingOffsets(OffsetsInitializer.offsets(offsets, OffsetResetStrategy.EARLIEST))
      .setProperties(props)
      .build()

    val stream: DataStream[GenericRecord] =
      env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), sourceName)

    stream
      .filter { elem =>
        (elem.get("_id").toString.contains("6278ab619a7e891717224502") ||
        elem.get("_id").toString.contains("6278ad4c4b58eae44ef02c1e"))
      }
      .print()

  }
}
