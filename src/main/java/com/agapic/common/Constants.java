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

package com.agapic.common;

/** A utility class to hold various constants
 * used by the Twitch Spark Streaming library.
 **/
public final class Constants {
    private Constants() {}

    // IRC config
    public static final String HOST = "irc.chat.twitch.tv";
    public static final int PORT = 6667;

    // Twitch API config
    public static final String STREAM_ENDPOINT = "https://api.twitch.tv/kraken/streams";

    // Generic string constants
    public static final String USERNAME = "twitch_username";
    public static final String PASSWORD = "twitch_password";
    public static final String NICK = "NICK";
    public static final String PASS = "PASS";
    public static final String SPACE = " ";
    public static final String DELIMITER = "\r\n";
    public static final String VALID_AUTH = "Welcome, GLHF!";

    // HTTP messages
    public static final int RESPONSE_OK = 200;

    // Error messages
    public static final String AUTH_FAILED = "Login authentication failed";
    public static final String BAD_AUTH_FORMAT = "Improperly formatted auth";
    public static final String AUTH_FAILED_GENERIC = "Failed to connect to the server.";
    public static final String BAD_CLIENT_ID = "Invalid Twitch API client id";

}
