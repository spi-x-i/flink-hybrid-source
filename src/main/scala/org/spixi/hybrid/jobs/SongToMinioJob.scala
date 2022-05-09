package org.spixi.hybrid.jobs

import io.radicalbit.FinalConverter_OutputGroup1_Schema
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.file.sink.FileSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.core.fs.Path
import org.apache.flink.formats.avro.AvroWriters
import org.apache.flink.streaming.api.functions.sink.filesystem.PartFileInfo
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.{ConsumerConfig, OffsetResetStrategy}
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.TopicPartition
import org.slf4j.Logger
import org.spixi.hybrid.configuration.Config
import org.spixi.hybrid.serde.{KafkaDeserializer, KafkaDeserializerCertilogo}

import scala.collection.JavaConverters._
import java.time.Clock
import java.{lang, util}
import java.util.Properties
import scala.collection.mutable

object SongToMinioJob {

  def apply(env: StreamExecutionEnvironment, config: Config)(implicit log: Logger, clock: Clock): Unit = {
    val kafkaConfig = config.kafkaConfig
    val s3Config    = config.s3Config
    val sourceName  = "certilogo spixi consumer"
    val sourceTopic =
      "PROC_65794a8b-510e-4c91-8e3a-36fcce64f81c_010a2188-545e-450c-bb62-358f6e575164_authentication-sink"
    val properties = new Properties()
    properties.put(ProducerConfig.CLIENT_ID_CONFIG, "Generator Sink")

    val offsets: util.Map[TopicPartition, java.lang.Long] = (0 to 47)
      .map { id =>
        val key   = new TopicPartition(sourceTopic, id)
        val value = if (id != 10) new lang.Long(5200000) else new lang.Long(8000000)
        key -> value
      }
      .toMap
      .asJava

    val props = new Properties()
    props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000)

    val kafkaSource = KafkaSource
      .builder[FinalConverter_OutputGroup1_Schema]()
      .setBootstrapServers(kafkaConfig.bootstrapServers)
      .setTopics(sourceTopic)
      .setDeserializer(new KafkaDeserializerCertilogo(kafkaConfig.registryUrl, sourceTopic))
      .setGroupId(sourceName)
      .setStartingOffsets(OffsetsInitializer.offsets(offsets, OffsetResetStrategy.EARLIEST))
      .setProperties(props)
      .build()

    val stream: DataStream[FinalConverter_OutputGroup1_Schema] =
      env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), sourceName)

    stream
      .filter(_.eventType.get == "SESSION")
      .filter { elem =>
        elem.newKey.nonEmpty && (elem.newKey.get.contains("6278a3699a7e8927d62244a4") ||
          elem.newKey.get.contains("6278aa5b4b58ea6625f02bfa") ||
          elem.newKey.get.contains("6278ab619a7e891717224502") ||
          elem.newKey.get.contains("6278ad4c4b58eae44ef02c1e"))
      }
      .print()

  }
}
