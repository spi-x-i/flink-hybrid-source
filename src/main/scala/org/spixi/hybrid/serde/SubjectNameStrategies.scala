package org.spixi.hybrid.serde

object SubjectNameStrategies {

  sealed trait SubjectNameStrategy

  case object TopicNameStrategy       extends SubjectNameStrategy
  case object RecordNameStrategy      extends SubjectNameStrategy
  case object TopicRecordNameStrategy extends SubjectNameStrategy

}
