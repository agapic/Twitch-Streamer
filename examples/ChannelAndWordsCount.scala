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

import com.andrewgapic.spark.streaming.TwitchStreamBuilder
import com.andrewgapic.stream.Message
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object ChannelAndWordsCount {

  def main(args: Array[String]) {

    // Convenience method to get auth
    setAuth()

    val sparkConf = new SparkConf().setAppName("TwitchTest")

    // check Spark configuration for master URL, set it to local if not configured
    if (!sparkConf.contains("spark.master")) {
      sparkConf.setMaster("local[2]")
    }
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val gamesList: Set[String] = Set("League+of+Legends")
    val stopWords = getStopWords()

    // Create the input DStream, and listen to messages on the game League of Legends.
    // Note: spaces in games must have "+" inserted.
    val stream: ReceiverInputDStream[Message] = new TwitchStreamBuilder()
      .setGames(gamesList).build(ssc)

    // Map each incoming message object to its channel
    val channels = stream.map(msg => msg.getChannel())

    // Map each incoming message object to its message content
    val messages = stream.map(msg => msg.getContent())

    // Tokenize a message's content, ignoring stopwords
    val words = messages.flatMap(msg => msg.split(" "))
    val cleanWords = words.filter(!stopWords.contains(_))

    // Do a simple map reduce based on (channel, count) tuples, then map to a (count, channel) tuple and sort by count.
    val topChannelActivityCounts15 = channels.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(600))
      .map { case (channel, count) => (count, channel) }
      .transform(_.sortByKey(false))

    val topMessageActivityCounts15 = cleanWords.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(600))
      .map { case (message, count) => (count, message) }
      .transform(_.sortByKey(false))

    // Take the top 15 results and print them
    topChannelActivityCounts15.foreachRDD(rdd => {
      val channelList = rdd.take(15)
      println("\nPopular channels in last 10 minutes (%s total):".format(rdd.count()))
      channelList.foreach { case (count, channel) => println("%s (%s messages)".format(channel, count)) }
    })

    topMessageActivityCounts15.foreachRDD(rdd => {
      val messageList = rdd.take(15)
      println("\nPopular words in last 10 minutes (%s total):".format(rdd.count()))
      messageList.foreach { case (count, message) => println("%s (%s messages)".format(message, count)) }
    })

    // Stop the streaming
    ssc.start()
    ssc.awaitTermination()
  }

  def setAuth() {
    import scala.io.Source
    for (line <- Source.fromFile("twitch_auth.txt").getLines) {
      val fields = line.split(" ")
      if (fields.length == 2) {
        System.setProperty(fields(0), fields(1))
      }
    }
  }

  def getStopWords(): Set[String] = {
    import scala.collection.mutable.ListBuffer
    val source = scala.io.Source.fromFile("stopwords.txt")
    val stopWords = new ListBuffer[String]()

    val lines = try {
      source.getLines.foreach(stopWords += _)
    } finally {
      source.close()
    }

    return stopWords.toSet
  }
}
