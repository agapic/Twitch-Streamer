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

package com.agapic.spark.streaming

import java.io.IOException

import akka.actor.ActorSystem
import com.agapic.stream.{Message, TwitchStream}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream._
import org.apache.spark.streaming.receiver.Receiver
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/** A stream of Twitch messages.
  *
  *
  * @constructor Creates a new TwitchStream object and streams in Message objects.
  *
  * If no games or channels are provided, the program terminates as no messages will be streamed.
  * It's recommended to have a longer scheduling interval as a short one will slow things down due to making an API
  * request to Twitch. Also, the rate of large channels being online and subsequently offline is relatively slow.
  *
  * TODO: make all async operations have their own thread for performance.
  */
private[streaming]
class TwitchInputDStream(
    ssc: StreamingContext,
    games: Set[String],
    channels: Set[String],
    language: String,
    storageLevel: StorageLevel,
    schedulingInterval: FiniteDuration) extends ReceiverInputDStream[Message](ssc) {

  override def getReceiver(): Receiver[Message] = {
    new TwitchReceiver(games, channels, language, storageLevel, schedulingInterval)
  }
}

class TwitchReceiver(
    games: Set[String],
    channels: Set[String],
    language: String,
    storageLevel: StorageLevel,
    schedulingInterval: FiniteDuration) extends Receiver[Message](storageLevel) {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[TwitchInputDStream])

  @volatile private var twitchStream: TwitchStream = _

  def onStart() {
    // Start the thread that receives data over a connection
    new Thread("Socket Receiver") {
      override def run() {
        receive()
      }
    }.start()
  }

  private def receive() {
    var message: Message = null
    val gamesList : java.util.Set[String] = games.asJava
    val channelList : java.util.Set[String] = channels.asJava


    try {
      twitchStream = new TwitchStream
      val system : ActorSystem = ActorSystem.apply("system")

      if (!channelList.isEmpty) {
        twitchStream.joinIndividualChannels(channelList)
      }

      if (!gamesList.isEmpty) {
        system.scheduler.schedule(0 seconds, schedulingInterval)(joinGameChannelsHandler(gamesList))
      }
      message = twitchStream.getMessage

      while (!isStopped) {
        if (message != null) {
          LOGGER.debug("Message={}, Author={}, Channel={}", message.getContent, message.getAuthor, message.getChannel)
          store(message)
        }
        message = twitchStream.getMessage
      }

      twitchStream.closeIO()
      // Restart in an attempt to connect again when server is active again
      restart("Trying to connect again")
    } catch {
      case a: javax.naming.AuthenticationException =>
        LOGGER.error("Invalid authentication to Twitch IRC Chat={}", a)
        System.exit(1);
      case b: javax.ws.rs.BadRequestException =>
        LOGGER.error("Bad request to TwitchApi={}", b)
        System.exit(1);
      case e: java.net.ConnectException =>
        LOGGER.error("Error connecting to " + "irc.chat.twitch.tv" + ":" + 6667)
        restart("Error connecting to " + "irc.chat.twitch.tv" + ":" + 6667, e)
      case t: Throwable =>
        LOGGER.error("Error receiving data")
        restart("Error receiving data", t)
    }
  }

  def onStop() {
    LOGGER.warn("Application was stopped.")
    if (twitchStream != null) {
      twitchStream.closeIO()
    }
  }

  /**
    * Akka actor handler. This is called once every n seconds, as determined by reschedulingInterval.
    * @param gamesList
    */
  @throws(classOf[IOException])
  def joinGameChannelsHandler(gamesList:java.util.Set[String]) {
      try {
        twitchStream.joinGames(gamesList, language)
        if (twitchStream.getJoinedGameChannels.isEmpty) {
          LOGGER.error("The input games are likely non-existent on Twitch.")
          throw new RuntimeException("No channels joined.")
        }
      } catch {
        case i: java.io.IOException =>
          LOGGER.error("IO Stream closed unexpectedly; likely when closing Spark.")
          System.exit(1)
      }
  }
}
