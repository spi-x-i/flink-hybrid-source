package org.spixi.hybrid.serde

import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.kafka.clients.producer.ProducerRecord
import org.spixi.hybrid.SongPlayed
import org.spixi.hybrid.serde.Serde.AvroValueSerialiser

import java.lang

final class KafkaSerializer(registryURL: String, topic: String) extends KafkaRecordSerializationSchema[SongPlayed] {

  private lazy val avroSer = AvroValueSerialiser(registryURL)

  override def serialize(
      element: SongPlayed,
      context: KafkaRecordSerializationSchema.KafkaSinkContext,
      timestamp: lang.Long
  ): ProducerRecord[Array[Byte], Array[Byte]] = {
    val key   = element.song_played.getBytes
    val value = avroSer.serialize[SongPlayed](topic, element)
    new ProducerRecord[Array[Byte], Array[Byte]](topic, key, value)
  }
}
