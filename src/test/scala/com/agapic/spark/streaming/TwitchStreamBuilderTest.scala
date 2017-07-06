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

import com.agapic.stream.Message
import org.apache.spark.SparkFunSuite
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}
import org.scalatest.BeforeAndAfter

import scala.concurrent.duration._

class TwitchStreamBuilderTest extends SparkFunSuite with BeforeAndAfter {
  private val master: String = "local[2]"
  private val framework: String = this.getClass.getSimpleName
  private val batchDuration: Duration = Seconds(2)
  test("Building Twitch Stream") {
    val ssc = new StreamingContext(master, framework, batchDuration)
    val gamesList: Set[String] = Set("League+of+Legends")
    val schedulingInterval: FiniteDuration = 5.seconds

    val stream: ReceiverInputDStream[Message] = new TwitchStreamBuilder()
      .setGames(gamesList)
      .setSchedulingInterval(schedulingInterval)
      .build(ssc)

    ssc.stop()
  }

}
