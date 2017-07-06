# Twitch-Streamer: use Spark Streaming to to stream messages from Twitch.tv's IRC chat

`Twitch-Streamer` uses Twitch's [Chat and IRC API](https://dev.twitch.tv/docs/v5/guides/irc/) to stream in messages from specified
Twitch.tv channels. `Twitch-Streamer` is a light-weight wrapper over Spark Streaming and Twitch's Chat IRC data feed. The goal of this project is to utilize the strengths of Spark Streaming for the analysis of Twitch's live stream chatrooms.

## Full Documentation
TODO: put javadoc here

## Communication
- Twitter: [@AndrewGapic](http://twitter.com/andrewgapic)
- [GitHub Issues](https://github.com/agapic/twitch-streamer/issues)

## Getting Started
TODO: place Maven and SBT dependency here once settled on central repo.

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
### twitch_auth.txt
1. You can obtain the `twitch_client_id` by registering for your application here: https://www.twitch.tv/settings/connections. Alternatively, you can follow the instructions here: https://blog.twitch.tv/client-id-required-for-kraken-api-calls-afbb8e95f843.
2. Your `twitch_username` can be any string that hasn't joined the IRC chat already. Typically, I just use my actual username.
3. Your `twitch_password` can be retrieved here: ttp://twitchapps.com/tmi/ after your application has been registered.


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
