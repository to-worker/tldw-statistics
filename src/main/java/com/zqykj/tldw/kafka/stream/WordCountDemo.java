package com.zqykj.tldw.kafka.stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author feng.wei
 * @date 2018/6/1
 */
public class WordCountDemo {

    private static Logger logger = LoggerFactory.getLogger(WordCountDemo.class);

    public static Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-WordCountDemo");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "172.30.6.14:9092");
        props.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "172.30.6.14:2181");
        props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    public static void main(String[] args) {
        Long windowSize = 1000 * 10L;
        Long interval = windowSize;
        KStreamBuilder builder = new KStreamBuilder();
        KStream<String, String> source = builder.stream("test");
        KTable<Windowed<String>, Long> countTable = source.flatMapValues(new ValueMapper<String, Iterable<String>>() {
            @Override
            public Iterable<String> apply(String value) {
                return Arrays.asList(value.split(" "));
            }
        }).map(new KeyValueMapper<String, String, KeyValue<String, String>>() {
            @Override
            public KeyValue<String, String> apply(String key, String value) {
                return new KeyValue<>(value, value);
            }
        }).countByKey(TimeWindows.of("window-wordcount", windowSize).advanceBy(interval));

        countTable.foreach(new ForeachAction<Windowed<String>, Long>() {
            @Override
            public void apply(Windowed<String> key, Long value) {
                System.out.println("key:" + key.key() + ", value:" + value);
            }
        });

        KafkaStreams streams = new KafkaStreams(builder, getProperties());
        streams.start();
        //streams.close();
    }
}
