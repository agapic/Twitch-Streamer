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

package com.agapic.twitch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

/**
 * A POJO that holds converted JSON data from Twitch API responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelContainer {
    /**
     * Stores the streams/channels obtained from the Twitch API games endpoint.
     */
    @JsonProperty("streams")
    private Set<String> channels = null;

    public ChannelContainer() {
        channels = new HashSet<String>();
    }

    @SuppressWarnings("unchecked")
    public void setStreams(final List<LinkedHashMap<String, Object>> streams) {
        for (LinkedHashMap<String, Object> map : streams) {
            Map<String, Object> channel = (LinkedHashMap<String, Object>) map.get("channel");
            String name = (String) channel.get("name");
            channels.add("#" + name);
        }
    }

    public Set<String> getChannels() {
        return channels;
    }
}
