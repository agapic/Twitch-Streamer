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

package com.agapic.stream;

import com.agapic.twitch.TwitchApi;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Wrapper class that wraps the IRC server and the Twitch API.
 * All spark streaming related classes interact with the API and IRC server through this.
 * Stores a "state" of the current channels that are joined.
 */
public class TwitchStream {

    private static TwitchApi twitchApi;
    private IRC irc;
    /**
     * The set of channels a user has joined from games.
     * Changes over time to keep things fresh.
     * See the schedulingInterval in TwitchStreamBuilder for more information.
     */
    private Set<String> joinedGameChannels;

    /**
     * Specific channels a user may have opted to join, not tied to any game.
     * These will persist throughout the entirety of the streaming session.
     */
    private Set<String> joinedIndividualChannels;

    public TwitchStream() throws IOException, AuthenticationException {
        irc = new IRC();
        twitchApi = new TwitchApi();
        joinedGameChannels = new HashSet<>();
        joinedIndividualChannels = new HashSet<>();
    }

    /**
     * Reads a line of text sent downstream from IRC.
     * Sends a PONG response to IRC if a PING was sent downstream, to keep the connection alive.
     *
     * @return a new message constructed from the line read
     * @throws IOException
     */
    public Message getMessage() throws IOException {
        String input = irc.getReader().readLine();
        if (input.startsWith("PING")) {
            irc.write("PONG :tmi.twitch.tv\r\n");
        }
        return new Message().createMessage(input);
    }

    /**
     * Close all IO streams and sockets.
     *
     * @throws IOException
     */
    public void closeIO() throws IOException {
        irc.closeAll();
    }

    /**
     * Calls the Twitch web API to return (by default) the top 25 streams/channels for the specified games.
     * Leaves all previous channels
     *
     * @param games    a list of games, i.e. "League+of+Legends"
     *                 TODO: replace spaces with "+" for convenience
     * @param language the language that the channel is currently in. Default is "en" for English.
     * @throws IOException
     */
    public void joinGames(final Set<String> games, final String language) throws IOException {
        Set<String> channels = new HashSet<>();
        for (String game : games) {
            Set<String> channelsForGame = twitchApi.getStreamsByGame(game, language);
            if (channelsForGame.isEmpty()) {
                continue;
            }
            channels.addAll(channelsForGame);
        }
        //channels = channels.stream().map(e -> "#" + e).collect(Collectors.toSet());
        regulateGameChannels(channels);

    }

    /**
     * Helper function to convert a channel into an input accepted by IRC.
     * Each channel should consist of a "#" followed by the stream name in lower case.
     * Example: "TSM_Dyrus" -> "#tsm_dyrus"
     *
     * @param channel the channel name to format
     * @return the formatted channel
     */
    private String formatChannel(final String channel) {
        assert (channel != null);
        String formattedChannel = channel.toLowerCase();
        if (!channel.startsWith("#")) {
            return "#" + formattedChannel;
        }
        return formattedChannel;
    }

    /**
     * Leaves all old channels that were joined from games and joins new ones.
     * This ensures that offline channels are left and newer channels (with potentially lots of activity) are
     * capitalized on.
     * The channels are assumed to be formatted correctly.
     *
     * @param channels
     * @throws IOException
     */
    private void regulateGameChannels(final Set<String> channels) throws IOException {
        irc.leaveChannels(joinedGameChannels);
        //final Set<String> formattedChannels = channels.stream().map(this::formatChannel).collect(Collectors.toSet());
        joinedGameChannels.addAll(channels);
        irc.joinChannels(joinedGameChannels);
        joinedGameChannels = channels;
    }

    /**
     * Joins user-specified channels.
     *
     * @param channels the set of channels a user wants to join
     * @throws IOException
     */
    public void joinIndividualChannels(final Set<String> channels) throws IOException {
        final Set<String> formattedChannels = channels.stream().map(this::formatChannel).collect(Collectors.toSet());
        irc.joinChannels(formattedChannels);
        this.joinedIndividualChannels = formattedChannels;
    }

    public Set<String> getJoinedGameChannels() {
        return joinedGameChannels;
    }

}
