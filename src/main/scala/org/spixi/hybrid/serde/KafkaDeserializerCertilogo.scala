package org.spixi.hybrid.serde

import io.radicalbit.FinalConverter_OutputGroup1_Schema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema
import org.apache.flink.util.Collector
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.spixi.hybrid.SongPlayed
import org.spixi.hybrid.serde.Serde.AvroValueDeserialiser

final class KafkaDeserializerCertilogo(registryURL: String, topic: String)
    extends KafkaRecordDeserializationSchema[FinalConverter_OutputGroup1_Schema] {

  private lazy val avroDes = AvroValueDeserialiser(registryURL)

  override def deserialize(
      record: ConsumerRecord[Array[Byte], Array[Byte]],
      out: Collector[FinalConverter_OutputGroup1_Schema]
  ): Unit = {
    val value = avroDes.deserialize[FinalConverter_OutputGroup1_Schema](topic, record.value())
    out.collect(value)
  }

  override def getProducedType: TypeInformation[FinalConverter_OutputGroup1_Schema] =
    TypeInformation.of(classOf[FinalConverter_OutputGroup1_Schema])

}
