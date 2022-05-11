package org.spixi.hybrid.serde

import org.apache.avro.generic.GenericRecord
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.spixi.hybrid.serde.Serde.AvroValueDeserialiser

final class KafkaDeserializerCertilogoGeneric(registryURL: String, topic: String)
    extends KafkaRecordDeserializationSchema[GenericRecord] {

  private lazy val avroDes = AvroValueDeserialiser(registryURL)

  override def deserialize(
      record: ConsumerRecord[Array[Byte], Array[Byte]],
      out: Collector[GenericRecord]
  ): Unit = {
    val value = avroDes.deserialize[GenericRecord](topic, record.value())
    out.collect(value)
  }

  override def getProducedType: TypeInformation[GenericRecord] =
    TypeInformation.of(classOf[GenericRecord])

}
