package org.spixi.hybrid.jobs

import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.connector.file.sink.FileSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.core.fs.Path
import org.apache.flink.formats.avro.AvroWriters
import org.apache.flink.streaming.api.functions.sink.filesystem.PartFileInfo
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy
import org.apache.flink.streaming.api.scala._
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.ProducerConfig
import org.slf4j.Logger
import org.spixi.hybrid.SongPlayed
import org.spixi.hybrid.configuration.Config
import org.spixi.hybrid.serde.KafkaDeserializer

import java.time.Clock
import java.util.Properties

object SongToMinioJob {

  def apply(env: StreamExecutionEnvironment, config: Config)(implicit log: Logger, clock: Clock): Unit = {
    val kafkaConfig = config.kafkaConfig
    val s3Config    = config.s3Config
    val sourceName  = "SongsToMinio Source"
    val properties  = new Properties()
    properties.put(ProducerConfig.CLIENT_ID_CONFIG, "Generator Sink")

    val kafkaSource = KafkaSource
      .builder[SongPlayed]()
      .setBootstrapServers(kafkaConfig.bootstrapServers)
      .setTopics(config.kafkaConfig.generatorTopic)
      .setDeserializer(new KafkaDeserializer(kafkaConfig.registryUrl, kafkaConfig.generatorTopic))
      .setGroupId(sourceName)
      .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
      .build()

    val stream: DataStream[SongPlayed] = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), sourceName)

    val sink: FileSink[SongPlayed] = FileSink
      .forBulkFormat[SongPlayed](new Path(s3Config.outputPath), AvroWriters.forSpecificRecord(classOf[SongPlayed]))
      .withRollingPolicy(
        new CheckpointRollingPolicy[SongPlayed, String]() {
          override def shouldRollOnEvent(partFileState: PartFileInfo[String], element: SongPlayed): Boolean = false
          override def shouldRollOnProcessingTime(partFileState: PartFileInfo[String], currentTime: Long): Boolean =
            currentTime - partFileState.getCreationTime > 1000 * 60 * 10
        }
      )
      .build()

    stream.sinkTo(sink)

  }
}
