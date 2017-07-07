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

import com.andrewgapic.spark.streaming.TwitchStreamBuilder;
import com.andrewgapic.stream.Message;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class JavaWordsCount {

    public static void main(final String[] args) {

        setAuth();
        SparkConf sparkConf = new SparkConf().setAppName("TwitchTest");

        // check Spark configuration for master URL, set it to local if not configured
        if (!sparkConf.contains("spark.master")) {
            sparkConf.setMaster("local[2]");
        }
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(2000));

        Set<String> games = new HashSet<>();
        games.add("League+of+Legends");
        String language = "en";
        Set<String> stopWords = getStopWords();

        JavaReceiverInputDStream<Message> stream = new TwitchStreamBuilder().setGames(games).build(jssc);

        // Map each incoming message object to its message content
        JavaDStream<String> messages = stream.map(new Function<Message, String>() {
            public String call(final Message msg) {
                return msg.getContent();
            }
        });

        // Tokenize a message's content, ignoring stopwords
        JavaDStream<String> words = messages.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(final String s) {
                return Arrays.asList(s.split(" ")).iterator();
            }
        });

        JavaDStream<String> cleanWords = words.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(final String word) {
                return stopWords != null && !stopWords.contains(word);
            }
        });

        // map words to (word, 1)
        JavaPairDStream<String, Integer> cleanWordsCount = cleanWords.mapToPair(
                new PairFunction<String, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(final String s) {
                        return new Tuple2<>(s, 1);
                    }
                });

        // Reduce (calculate totals)
        JavaPairDStream<String, Integer> cleanWordsCountTotals = cleanWordsCount.reduceByKeyAndWindow(
                new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(final Integer a, final Integer b) {
                        return a + b;
                    }
                }, new Duration(6000));

        // Map (word, count) to (count, word)
        JavaPairDStream<Integer, String> cleanWordsCountPairs = cleanWordsCountTotals.mapToPair(
                new PairFunction<Tuple2<String, Integer>, Integer, String>() {
                    @Override
                    public Tuple2<Integer, String> call(final Tuple2<String, Integer> wordPair) {
                        return new Tuple2<>(wordPair._2(),
                                wordPair._1());
                    }
                });

        // sort by frequency
        JavaPairDStream<Integer, String> top15Words = cleanWordsCountPairs.transformToPair(
                new Function<JavaPairRDD<Integer, String>, JavaPairRDD<Integer, String>>() {
                    @Override
                    public JavaPairRDD<Integer, String> call(
                            final JavaPairRDD<Integer, String> words) {
                        return words.sortByKey(false);
                    }
                }
        );

        // Take the top 15 results and print them
        top15Words.foreachRDD(new VoidFunction<JavaPairRDD<Integer, String>>() {
            @Override
            public void call(final JavaPairRDD<Integer, String> rdd) {
                List<Tuple2<Integer, String>> messageList = rdd.take(15);
                System.out.println(String.format("\nPopular words in last 10 minutes (%s total):", String.valueOf(rdd.count())));
                for (Tuple2<Integer, String> pair : messageList) {
                    System.out.println(
                            String.format("%s (%s messages)", pair._2(), pair._1()));
                }
            }
        });

        jssc.start();
        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void setAuth() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("twitch_auth.txt"));
            String line;
            List<String> lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                lines.add(line.split(" ")[1]);
            }
            reader.close();
            System.setProperty("twitch_client_id", lines.get(0));
            System.setProperty("twitch_username", lines.get(1));
            System.setProperty("twitch_password", lines.get(2));
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", "twitch_auth.txt");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Set<String> getStopWords() {
        Set<String> stopWords = new HashSet<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line);
            }
            reader.close();
        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", "stopwords.txt");
            e.printStackTrace();
            return null;
        }

        return stopWords;
    }
}


