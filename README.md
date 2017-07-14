# Twitch-Streamer: live stream Twitch.tv data with Spark Streaming

[![][license img]][license]

`Twitch-Streamer` uses Twitch's [Chat and IRC API](https://dev.twitch.tv/docs/v5/guides/irc/) to stream in messages from specified
Twitch.tv channels. `Twitch-Streamer` is a light-weight wrapper over Spark Streaming and Twitch's Chat IRC data feed. The goal of this project is to utilize the strengths of Spark Streaming for the analysis of Twitch's live stream chatrooms.

## Full Documentation
### Scaladocs
Clients (yourself!) will interact directly using the scala classes -- these are important! [Find them here](http://andrewgapic.com/spark-streaming-twitch/scaladocs/#com.andrewgapic.package)
### Javadocs
The inner working of this project were written in Java. [Find them here](http://andrewgapic.com/spark-streaming-twitch/javadocs/)

## Communication
- Twitter: [@AndrewGapic](http://twitter.com/andrewgapic)
- [GitHub Issues](https://github.com/agapic/twitch-streamer/issues)

## Getting Started
### Using Maven
```
<dependency>
    <groupId>com.andrewgapic</groupId>
    <artifactId>spark-streaming-twitch</artifactId>
    <version>1.0.0</version>
</dependency>
```
### Using SBT 
```
libraryDependencies += "com.andrewgapic" %% "spark-streaming-twitch" % "1.0.0"
```

## Developing
### Built With
This project is built with Java 1.8, Scala 2.11.8, and Spark Streaming 2.11.8.
### Build
```
$ git clone https://github.com/agapic/twitch-streamer.git
$ cd twitch-streamer/
$ mvn clean install
```
**Note**: If you're interested in helping with developing the project further, there are lots of features and optimizations that could be done. For example, concurrency isn't as optimal as it could be; the entire stream is being fed through one thread. Other asynchronous
calls could potentially also have their own thread. 

## Usage
Twitch-Streamer can be used with either Scala or Java, and was built with this notion in mind. Generally, Scala is a better fit since Spark was built in Scala, but as always, use your favourite language.

### twitch_auth.txt
1. You can obtain the `twitch_client_id` by registering for your application here: https://www.twitch.tv/settings/connections. Alternatively, you can follow the instructions here: https://blog.twitch.tv/client-id-required-for-kraken-api-calls-afbb8e95f843.
2. Your `twitch_username` can be any string that hasn't joined the IRC chat already. Typically, I just use my actual username.
3. Your `twitch_password` can be retrieved here: ttp://twitchapps.com/tmi/ after your application has been registered.

```
twitch_client_id <clientid>
twitch_username <username>
twitch_password <go here: http://twitchapps.com/tmi/> It's an oauth: password, not your twitch account password.
```

### Messages
Twitch-Streamer introduces an abstraction called a `Message`. It transforms a line of text from Twitch's IRC chat into a 
`Message`, which allows clients to get the author of the message, the channel name, and the actual message content.

### Scala API
```
import com.andrewgapic.spark.streaming.TwitchStreamBuilder
import com.andrewgapic.stream.Message
val gamesSet: Set[String] = Set("League+of+Legends")
val stream: ReceiverInputDStream[Message] = new TwitchStreamBuilder().setGames(gamesSet).build(ssc)
```

### Java API
```
import com.andrewgapic.spark.streaming.TwitchStreamBuilder;
import com.andrewgapic.stream.Message;
Set<String> gamesSet = new HashSet<>();
gamesSet.add("League+of+Legends");
JavaReceiverInputDStream<Message> stream = new TwitchStreamBuilder().setGames(gamesSet).build(jssc);
```

### More advanced usage (Scala)
**Note**: spaces in game names must be replaced by a `+` character. This will be done automatically in future versions.
```
import com.andrewgapic.spark.streaming.TwitchStreamBuilder
import com.andrewgapic.stream.Message
val sparkConf = new SparkConf().setAppName("TwitchTest")
val ssc = new StreamingContext(sparkConf, Seconds(2))
val gamesSet: Set[String] = Set("League+of+Legends")
val channelsSet: Set[String] = Set("TSM_Dyrus")
val language: String = "en" //english
val storageLevel: StorageLevel = StorageLevel.MEMORY_AND_DISK_SER_2
val schedulingInterval: FiniteDuration = 600 seconds // refresh channels every 10 minutes
val stream: ReceiverInputDStream[Message] = new TwitchStreamBuilder()
                                            .setGames(gamesSet)
                                            .setChannels(channelsSet)
                                            .setLanguage(language)
                                            .setStorageLevel(storageLevel)
                                            .setSchedulingInterval(schedulingInterval)
                                            .build(ssc)
```

## Examples
There are two examples in the `examples` folder; one in Scala (`ChannelAndWordsCount`), and one in Java (`JavaWordsCount`).
The Scala example does two things: displays the top 15 words by word frequency (ignores stopwords), and displays the top channels by message frequency. The Java example only displays word frequency.


## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/agapic/twitch-streamer/issues).

## LICENSE

Copyright 2017, Andrew Gapic.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[license]:LICENSE
[license img]:https://img.shields.io/badge/License-Apache%202-blue.svg
