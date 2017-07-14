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

import com.andrewgapic.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.AuthenticationException;
import java.io.*;
import java.net.Socket;
import java.util.Set;

/**
 * A small class that handles actions that interact directly with the Twitch IRC server.
 */
public final class IRC {

    private static final Logger LOGGER = LoggerFactory.getLogger(IRC.class);
    /**
     * Blocking socket. TODO: perhaps create a non-blocking socket?
     */
    private Socket socket;
    /**
     * Used for reading messages from the IRC server.
     */
    private BufferedReader reader;
    /**
     * Used for writing to the IRC server.
     */
    private BufferedWriter writer;

    /**
     * Connect to the IRC server, send authentication, and verify connection.
     *
     * @throws IOException             if the writer/reader fails
     * @throws AuthenticationException if the server failed to authenticate the user
     */
    IRC() throws IOException, AuthenticationException {
        LOGGER.info("Connecting to IRC server. Host={}, Port={}", Constants.HOST, Constants.PORT);
        socket = new Socket(Constants.HOST, Constants.PORT);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        sendAuth();
        checkAuth(reader.readLine());
    }

    /**
     * Sends the username and password to Twitch.
     * TODO: create a random NICK if one is not provided,
     * TODO: allow user to enter username and password in the java client as opposed to a file
     *
     * @throws IOException             if writer fails
     * @throws AuthenticationException if password is invalid
     */
    private void sendAuth() throws IOException, AuthenticationException {
        StringBuilder sb = new StringBuilder();
        String password = System.getProperty(Constants.PASSWORD);
        String username = System.getProperty(Constants.USERNAME);
        sb.append(Constants.PASS).append(Constants.SPACE).append(password).append(Constants.DELIMITER)
                .append(Constants.NICK).append(Constants.SPACE).append(username).append(Constants.DELIMITER);
        write(sb.toString());
    }
    // TODO: check all the different responses that Twitch gives

    /**
     * @param response is the first line read after the user logs in. If it fails, close the socket, writer, and reader.
     * @throws AuthenticationException Invalid oauth key.
     * @throws IOException
     */
    private void checkAuth(final String response) throws AuthenticationException, IOException {
        if (response.endsWith(Constants.AUTH_FAILED)) {
            closeAll();
            throw new AuthenticationException(Constants.AUTH_FAILED);
        }

        if (response.endsWith(Constants.BAD_AUTH_FORMAT)) {
            closeAll();
            throw new AuthenticationException(Constants.BAD_AUTH_FORMAT);
        }

        if (!response.endsWith(Constants.VALID_AUTH)) {
            closeAll();
            throw new AuthenticationException(Constants.AUTH_FAILED_GENERIC);
        }
    }

    /**
     * Close the socket, reader, and writer gracefully.
     *
     * @throws IOException
     */
    public void closeAll() throws IOException {
        socket.close();
        reader.close();
        writer.close();
    }

    /**
     * Joins a set of unique channels. The IRC server will send messages in these channels.
     *
     * @param channels a set of correctly formatted channels. Should take the form of: Set("#channel1","#channel2",...)
     * @throws IOException
     */
    void joinChannels(final Set<String> channels) throws IOException {
        if (!channels.isEmpty()) {
            String s = String.join(",", channels);
            write("JOIN " + s + "\r\n");
        }
    }

    /**
     * Leaves a set of unique channels. Messages will not be received from these channels anymore.
     *
     * @param channels a set of correctly formatted channels. Should take the form of: Set("#channel1","#channel2",...)
     * @throws IOException
     */
    void leaveChannels(final Set<String> channels) throws IOException {
        if (!channels.isEmpty()) {
            String s = String.join(",", channels);
            write("PART " + s + "\r\n");
        }
    }

    /**
     * Wrapper function to write a string and flush it.
     *
     * @param value The string value to write to the IRC server.
     * @throws IOException
     */
    public void write(final String value) throws IOException {
        writer.write(value);
        writer.flush();
    }

    BufferedReader getReader() {
        return reader;
    }

}
