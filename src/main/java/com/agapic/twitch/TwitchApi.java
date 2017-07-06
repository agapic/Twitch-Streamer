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

import com.agapic.common.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.BadRequestException;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import static com.agapic.common.Constants.BAD_CLIENT_ID;
import static com.agapic.common.Constants.STREAM_ENDPOINT;

/**
 * API Wrapper that handles requests to Twitch's API.
 */
public class TwitchApi {

    private static final String GAME_PARAM = "game";
    private static final String LANG_PARAM = "lang";
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUALS = "=";
    private static final String CLIENT_ID_HEADER = "Client-ID";
    /**
     * Used for sending API requests to Twitch.
     */
    private static String clientId = null;
    private static ObjectMapper mapper;
    private static ChannelContainer channels;

    public TwitchApi() {
        mapper = new ObjectMapper();
        channels = new ChannelContainer();
        clientId = System.getProperty("twitch_client_id");
        assert (clientId != null);
    }

    /**
     * Filter streams/channels by game and by language.
     * Passes the JSON response to a Java POJO for easier use.
     *
     * @param game     the game to get the channels for
     * @param language filter all channels by language
     * @return a list of the channels currently streaming for a given game + language
     * @throws IOException TODO: maybe handle the case for "all" languages
     *                     TODO: make this run in its own thread. See java.util.concurrency
     *                     TODO: organize the API better. This function is weak.
     */
    public Set<String> getStreamsByGame(final String game, final String language) throws IOException {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(STREAM_ENDPOINT)
                .append(QUESTION_MARK)
                .append(GAME_PARAM)
                .append(EQUALS)
                .append(game);

        if (language != null) {
            urlBuilder.append(AMPERSAND)
                    .append(LANG_PARAM)
                    .append(EQUALS)
                    .append(language);
        }

        String urlString = urlBuilder.toString();
        URL url = new URL(urlString);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setRequestProperty(CLIENT_ID_HEADER, clientId);

        if (con.getResponseCode() != Constants.RESPONSE_OK) {
            throw new BadRequestException(BAD_CLIENT_ID + clientId);
        }

        channels = mapper.readValue(con.getInputStream(), ChannelContainer.class);
        return channels.getChannels();
    }
}
