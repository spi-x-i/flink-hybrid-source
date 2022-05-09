package org.spixi.hybrid.models

import org.spixi.hybrid.SongPlayed

import java.time.{Clock, Instant}
import java.util.UUID
import scala.util.Random

object EventGenerator {

  lazy val songs: Seq[String] = Seq(
    "Ciao Ciao",
    "Boring",
    "Let me go",
    "Goliath",
    "Vecchio mio",
    "Flying",
    "Dose",
    "Welcome to the party"
  )
  lazy val rnd                = new Random()
  def getSong: Option[String] = rnd.shuffle(EventGenerator.songs).headOption
}

final case class EventGenerator(maxSongs: Int, timestamp: Instant, songName: String) {
  private val throughput = scala.math.min(maxSongs / 60, 1)
  private var emitted    = 0

  private def isSecondLasted()(implicit clock: Clock): Boolean =
    clock.instant().toEpochMilli - timestamp.toEpochMilli > 999

  def getAndProgress(userUUID: UUID, username: String)(implicit clock: Clock): Either[Long, SongPlayed] =
    if (emitted < throughput && !isSecondLasted) {
      emitted = emitted + 1
      Right(SongPlayed(userUUID, username, songName, timestamp))
    } else {
      val elapsed = clock.instant().toEpochMilli - timestamp.toEpochMilli
      Left(1000L - elapsed)
    }
}
