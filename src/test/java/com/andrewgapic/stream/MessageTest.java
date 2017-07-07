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

import org.junit.Assert;
import org.junit.Test;

public class MessageTest {

    @Test
    public void testValidMessageConstruction() {
        String line = ":andrew!andrew@author.tmi.com.agapic.twitch.tv PRIVMSG #mrbean :hey man";
        Message msg = new Message().createMessage(line);
        Assert.assertNotNull(msg);
        Assert.assertEquals(msg.getAuthor(), "andrew");
        Assert.assertEquals(msg.getChannel(), "mrbean");
        Assert.assertEquals(msg.getContent(), "hey man");
    }

    @Test
    public void testInvalidMessageConstruction() {
        String line = "rubbish";
        Message msg = new Message().createMessage(line);
        Assert.assertNull(msg);
    }
}
