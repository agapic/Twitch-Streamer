
import com.agapic.spark.streaming.TwitchStreamBuilder
import com.agapic.stream.Message
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object ChannelAndWordsCount {

  def main(args: Array[String]) {

    // Convenience method to get auth
    setAuth()

    val sparkConf = new SparkConf().setAppName("TwitchTest")

    // check Spark configuration for master URL, set it to local if not configured
    if (!sparkConf.contains("spark.master")) {
      sparkConf.setMaster("local[2]")
    }
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    val gamesList: Set[String] = Set("League+of+Legends")
    val stopWords = getStopWords()

    // Create the input DStream, and listen to messages on the game League of Legends.
    // Note: spaces in games must have "+" inserted.
    val stream: ReceiverInputDStream[Message] = new TwitchStreamBuilder()
      .setGames(gamesList).build(ssc)

    // Map each incoming message object to its channel
    val channels = stream.map(msg => msg.getChannel())

    // Map each incoming message object to its message content
    val messages = stream.map(msg => msg.getContent())

    // Tokenize a message's content, ignoring stopwords
    val words = messages.flatMap(msg => msg.split(" "))
    val cleanWords = words.filter(!stopWords.contains(_))

    // Do a simple map reduce based on (channel, count) tuples, then map to a (count, channel) tuple and sort by count.
    val topChannelActivityCounts15 = channels.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(600))
      .map { case (channel, count) => (count, channel) }
      .transform(_.sortByKey(false))

    val topMessageActivityCounts15 = cleanWords.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(600))
      .map { case (message, count) => (count, message) }
      .transform(_.sortByKey(false))

    // Take the top 15 results and print them
    topChannelActivityCounts15.foreachRDD(rdd => {
      val channelList = rdd.take(15)
      println("\nPopular channels in last 10 minutes (%s total):".format(rdd.count()))
      channelList.foreach { case (count, channel) => println("%s (%s messages)".format(channel, count)) }
    })

    topMessageActivityCounts15.foreachRDD(rdd => {
      val messageList = rdd.take(15)
      println("\nPopular words in last 10 minutes (%s total):".format(rdd.count()))
      messageList.foreach { case (count, message) => println("%s (%s messages)".format(message, count)) }
    })

    // Stop the streaming
    ssc.start()
    ssc.awaitTermination()
  }

  def setAuth() {
    import scala.io.Source
    for (line <- Source.fromFile("real_twitch_auth.txt").getLines) {
      val fields = line.split(" ")
      if (fields.length == 2) {
        System.setProperty(fields(0), fields(1))
      }
    }
  }

  def getStopWords(): Set[String] = {
    import scala.collection.mutable.ListBuffer
    val source = scala.io.Source.fromFile("stopwords.txt")
    val stopWords = new ListBuffer[String]()

    val lines = try {
      source.getLines.foreach(stopWords += _)
    } finally {
      source.close()
    }

    return stopWords.toSet
  }
}
