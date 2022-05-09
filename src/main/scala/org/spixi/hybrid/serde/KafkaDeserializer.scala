package org.spixi.hybrid.serde

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.spixi.hybrid.SongPlayed
import org.spixi.hybrid.serde.Serde.AvroValueDeserialiser

final class KafkaDeserializer(registryURL: String, topic: String) extends KafkaRecordDeserializationSchema[SongPlayed] {

  private lazy val avroDes = AvroValueDeserialiser(registryURL)

  override def deserialize(record: ConsumerRecord[Array[Byte], Array[Byte]], out: Collector[SongPlayed]): Unit = {
    val value = avroDes.deserialize[SongPlayed](topic, record.value())
    out.collect(value)
  }

  override def getProducedType: TypeInformation[SongPlayed] = TypeInformation.of(classOf[SongPlayed])

}
