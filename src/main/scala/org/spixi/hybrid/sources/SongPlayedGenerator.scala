package org.spixi.hybrid.sources

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichParallelSourceFunction, SourceFunction}
import org.slf4j.Logger
import org.spixi.hybrid.models.EventGenerator
import org.spixi.hybrid.models.EventGenerator.getSong
import org.spixi.hybrid.{models, SongPlayed}

import java.time.Clock
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

final class SongPlayedGenerator(maxSongs: Int)(implicit clock: Clock, log: Logger)
    extends RichParallelSourceFunction[SongPlayed] {

  private lazy val cancelled: AtomicBoolean        = new AtomicBoolean(false)
  @transient private var generator: EventGenerator = _
  private val uuid                                 = UUID.randomUUID()
  private val username                             = "spixi"
  @transient private var song: String              = _

  private def getEvent: Either[Long, SongPlayed] = generator.getAndProgress(uuid, username)

  override def open(parameters: Configuration): Unit = {
    song = getSong.get
    generator = models.EventGenerator(maxSongs, clock.instant(), song)
    super.open(parameters)
  }

  override def run(ctx: SourceFunction.SourceContext[SongPlayed]): Unit = {
    log.info(s"Source generation event kickstarting.")
    while (!cancelled.get())
      ctx.getCheckpointLock.synchronized {
        getEvent match {
          case Right(event) =>
            log.debug(s"Writing event to kafka: \t $event")
            ctx.collectWithTimestamp(event, event.notifiedAt.toEpochMilli)
          case Left(waitTime) =>
            log.warn(s"Couldn't write event to kafka. Wait for ${waitTime}ms")
            Thread.sleep(waitTime)
            generator = models.EventGenerator(maxSongs, clock.instant(), getSong.get)

        }
      }
  }

  override def cancel(): Unit =
    cancelled.set(true)
}
