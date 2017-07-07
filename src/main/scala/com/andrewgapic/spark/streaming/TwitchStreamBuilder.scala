/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andrewgapic.spark.streaming

import java.util

import com.andrewgapic.stream.Message
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.api.java.{JavaReceiverInputDStream, JavaStreamingContext}
import org.apache.spark.streaming.dstream.ReceiverInputDStream

import scala.collection.JavaConverters._
import scala.concurrent.duration._

trait Builder {

  def setGames(games: util.Set[String]): Builder

  def setGames(games: Set[String]): Builder

  def setChannels(channels: util.Set[String]): Builder

  def setChannels(channels: Set[String]): Builder

  def setLanguage(language: String): Builder

  def setStorageLevel(storageLevel: StorageLevel): Builder

  def setSchedulingInterval(schedulingInterval: FiniteDuration): Builder

  def setSchedulingInterval[T <: Number](schedulingInterval: T): Builder

  def build(jssc: JavaStreamingContext): JavaReceiverInputDStream[Message]

  def build(ssc: StreamingContext): ReceiverInputDStream[Message]

}

/**
  * Builder class used to build a TwitchStream. The only mandatory fields to specify are ssc/jssc, and one of
  * channels or games.
  */
class TwitchStreamBuilder() extends Builder {
  private var games : Set[String] = Set.apply()
  private var channels : Set[String] = Set.apply()
  private var language : String = "en"
  private var storageLevel : StorageLevel = StorageLevel.MEMORY_AND_DISK_SER_2
  private var schedulingInterval : FiniteDuration = 600.seconds

  /** Use java.util.Set
    *
    * @param games joins the top 25 channels for each specified game
    * @return
    */
  override def setGames(games: util.Set[String]): TwitchStreamBuilder = {
    this.setGames(games.asScala.toSet)
    this
  }

  /**
    * See above. Uses a scala immutable set
    * @param games
    * @return
    */
  override def setGames(games: Set[String]): TwitchStreamBuilder = {
    this.games = games
    this
  }

  /** Uses java.util.Set
    *
    * @param channels join specific channels not tied to any games. This is persisted through the entire Spark session.
    * @return
    */
  override def setChannels(channels: util.Set[String]): TwitchStreamBuilder = {
    this.setChannels(channels.asScala.toSet)
    this
  }

  /**
    * See above. Uses a scala immutable set
    * @param channels
    * @return
    */
  override def setChannels(channels: Set[String]): TwitchStreamBuilder = {
    this.channels = channels
    this
  }

  /**
    *
    * @param language filter messages by language of the channel. Default = "en".
    * @return
    */
  override def setLanguage(language: String): TwitchStreamBuilder = {
    this.language = language
    this
  }

  /**
    *
    * @param storageLevel the storage level. See Spark documentation for more information.
    * @return
    */
  override def setStorageLevel(storageLevel: StorageLevel): TwitchStreamBuilder = {
    this.storageLevel = storageLevel
    this
  }

  /**
    *
    * @param schedulingInterval the frequency to "refresh" the list of channels in the games. An API request is made
    *                           to Twitch every **n** seconds, as specified by the scheduling interval. All previous
    *                           channels in those games are removed and replaced by a list of fresh channels in the game.
    *                           It's recommended to set this to 10 minutes (600 seconds) or more. This essentially
    *                           filters out channels that are offline and joins new, popular channels that came online
    *                           while the streaming session was running.
    * @return
    */
  override def setSchedulingInterval(schedulingInterval: FiniteDuration): TwitchStreamBuilder = {
    this.schedulingInterval = schedulingInterval
    this
  }

  /**
    * The same as above, but accepts any class that extends Number, for ease of use with Java, as FiniteDuration is a
    * Scala specific function.
    * @param schedulingInterval see above.
    * @tparam T Integer, Long, etc.
    * @return
    */
  override def setSchedulingInterval[T <: Number](schedulingInterval: T): TwitchStreamBuilder = {
    this.schedulingInterval = schedulingInterval.longValue().millis
    this
  }

  private def checkNotNull[T](ref: T, errorMessage: Object): T = {
    if (ref == null) throw new NullPointerException(String.valueOf(errorMessage))
    ref
  }

  /**
    * Builds a discretized spark stream for Twitch
    * @return
    */

  override def build(jssc: JavaStreamingContext): JavaReceiverInputDStream[Message] = {
    build(jssc.ssc)
  }

  override def build(ssc: StreamingContext): ReceiverInputDStream[Message] = {
    checkNotNull(ssc, "streaming context")
    checkNotNull(this.games, "games")
    checkNotNull(this.channels, "channels")
    checkNotNull(this.language, "language")
    checkNotNull(this.storageLevel, "storageLevel")
    checkNotNull(this.schedulingInterval, "scheduling interval")

    if (games.isEmpty && channels.isEmpty) {
      throw new IllegalStateException("No games or channels specified to connect to.")
    }
    new TwitchInputDStream(ssc, games, channels, language, storageLevel, schedulingInterval)

  }
}
