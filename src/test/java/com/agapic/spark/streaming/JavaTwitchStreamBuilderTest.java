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

package com.agapic.spark.streaming;

import com.agapic.stream.Message;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class JavaTwitchStreamBuilderTest extends LocalSparkStreaming {
    private static final String NO_GAMES_OR_CHANNELS = "No games or channels specified to connect to.";

    /**
     * Test that the flow works correctly
     */
    @Test
    public void testValidTwitchStreamBuilder() {
        Set<String> gamesList = new HashSet<>();
        gamesList.add("League+of+Legends");
        Set<String> channelsList = new HashSet<>();
        channelsList.add("#TSM_Dyrus");

        JavaReceiverInputDStream<Message> stream = new TwitchStreamBuilder()
                .setGames(gamesList)
                .setChannels(channelsList)
                .setLanguage("es")
                .setStorageLevel(StorageLevel.MEMORY_AND_DISK_SER_2())
                .setSchedulingInterval(300)
                .build(jssc);
    }

    @Test
    public void testNoChannelsOrGames() {
        try {
            JavaReceiverInputDStream<Message> stream = new TwitchStreamBuilder()
                    .setLanguage("es")
                    .setStorageLevel(StorageLevel.MEMORY_AND_DISK_SER_2())
                    .setSchedulingInterval(300)
                    .build(jssc);
        } catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), NO_GAMES_OR_CHANNELS);

        }
    }

    @Test
    public void failsOnNullParams() {
        try {
            JavaReceiverInputDStream<Message> stream = new TwitchStreamBuilder()
                    .setLanguage(null)
                    .build(jssc);
        } catch (NullPointerException e) {
            Assert.assertEquals(e.getMessage(), "language");
        }
    }
}
