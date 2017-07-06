# Twitch-Streamer: use Spark Streaming to to stream messages from Twitch.tv's IRC chat

[![][license img]][license]
`TwitchStreamBuilder` uses Twitch's [Chat and IRC API](https://dev.twitch.tv/docs/v5/guides/irc/) to stream in messages from specified
channels. `twitch-streamer` wraps Spark Streaming over the IRC client to allow users to take advantage of "scalable, high-throughput, 
fault-tolerant stream processing of live data streams". (See here: https://spark.apache.org/docs/latest/streaming-programming-guide.html#overview)
The `Message` object is an abstraction on top of lines being fed in.

## Getting Started
TODO: place Maven and SBT dependency here once settled on central repo.

## Developing
### Built With
This project is built with Java 1.8, Scala 2.11.8, and Spark Streaming 2.11.8.
### Setting up the Dev environment
If you're interested in helping with developing the project further, there are lots of features and optimizations that could be done.
For example, concurrency isn't as optimal as it could be; the entire stream is being fed through one thread. Other asynchronous
calls could potentially also have their own thread. 

```
git clone https://github.com/agapic/twitch-streamer.git
cd twitch-streamer/
mvn clean install
```

## Usage
The first thing you need to do is enter your credentials in the provided twitch_auth.txt.
You can obtain the `twitch_client_id` by registering for your application here: https://www.twitch.tv/settings/connections.
Alternatively, you can follow the instructions here: https://blog.twitch.tv/client-id-required-for-kraken-api-calls-afbb8e95f843.
Your `twitch_username` can be any string that hasn't joined the IRC chat already. Typically, I just use my actual username.
Your `twitch_password` can be retrieved here: ttp://twitchapps.com/tmi/ after your application has been registered.

```
twitch_client_id <clientid>
twitch_username <username>
twitch_password <go here: http://twitchapps.com/tmi/> It's an oauth: password, not your twitch account password.
```

### Scala API
```
import com.agapic.spark.streaming.TwitchStreamBuilder
import com.agapic.stream.Message
val gamesSet: Set[String] = Set("League+of+Legends")
val stream: ReceiverInputDStream[Message] = new TwitchStreamBuilder().setGames(gamesSet).build(ssc)
```

### Java API
```
import com.agapic.spark.streaming.TwitchStreamBuilder;
import com.agapic.stream.Message;
Set<String> gamesSet = new HashSet<>();
gamesSet.add("League+of+Legends");
JavaReceiverInputDStream<Message> stream = new TwitchStreamBuilder().setGames(gamesSet).build(jssc);
```
