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

package com.andrewgapic.stream;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstracts a message from IRC into a Message object, which distills a message
 * into the user that wrote it, the channel it was written on, and the contained content.
 */
public final class Message implements Comparable<Message>, Serializable {
    /**
     * Checks if a line received from IRC matches a pattern that resembles a user message in a channel.
     */
    private static final transient Pattern PATTERN = twitchChatMessagePattern();

    /**
     * The author of the message. I.e., the user that wrote the message.
     */
    private String author = null;

    /**
     * The channel that the message was sent to.
     */
    private String channel = null;

    /**
     * The actual content of the message.
     */
    private String content = null;

    /**
     * Dottie's True Blue Cafe
     * Sample line representing a message:
     * :author!author@author.tmi.twitch.tv PRIVMSG #channel :message
     *
     * @return
     */
    private static Pattern twitchChatMessagePattern() {
        String username = "(^:(.+)!)(.*)";
        String messageAndChannel = "(PRIVMSG #([a-zA-Z0-9_]+) :(.+))";
        String regex = username + messageAndChannel;
        return Pattern.compile(regex);
    }

    /**
     * Constructs the message object based on a pattern and a line
     *
     * @param line is a string received from IRC
     * @return
     */
    Message createMessage(final String line) {
        Matcher matcher = PATTERN.matcher(line);
        if (matcher.matches()) {
            this.channel = matcher.group(5);
            this.author = matcher.group(2);
            this.content = matcher.group(6);
            return this;
        }
        return null;
    }

    public String getAuthor() {
        return author;
    }

    public String getChannel() {
        return channel;
    }

    public String getContent() {
        return content;
    }

    /**
     * Sorts messages by channel name as of now.
     *
     * @param that
     * @return
     */
    public int compareTo(final Message that) {
        if (that == null || that.getChannel() == null) {
            return -1;
        }

        return this.getChannel().compareToIgnoreCase(that.getChannel());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (!author.equals(message.author)) return false;
        return channel.equals(message.channel) && content.equals(message.content);
    }

    @Override
    public int hashCode() {
        int result = author.hashCode();
        result = 31 * result + channel.hashCode();
        result = 31 * result + content.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "author='" + author + '\'' +
                ", channel='" + channel + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
