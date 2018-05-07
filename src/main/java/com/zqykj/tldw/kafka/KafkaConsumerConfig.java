package com.zqykj.tldw.kafka;

import com.sun.corba.se.impl.orbutil.closure.Constant;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import com.zqykj.tldw.streaming.common.Constants;
import com.zqykj.tldw.streaming.entity.BussinessStatistics;
import com.zqykj.tldw.streaming.entity.SumStatistics;
import com.zqykj.tldw.streaming.listener.KafkaBussinessListeners;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author feng.wei
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap.brokers}")
    private String brokers;

    @Value("${spring.kafka.consumer.group-id}")
    private String group;

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(4);
        factory.getContainerProperties().setPollTimeout(4000);
        return factory;
    }

    @Bean
    public KafkaListeners kafkaListeners() {
        return new KafkaListeners();
    }

    /**
     * deal with the statistis info of streaming business
     *
     * @return
     */
    @Bean
    public KafkaBussinessListeners kafkaBussinessListeners() {
        Map<String, SumStatistics> sumStatisticsMap = new HashMap<>();
        Map<String, BussinessStatistics> bussinessStatisticsMap = new HashMap<>();
        bussinessStatisticsMap.put(Constants.METRICS_TRANS, new BussinessStatistics());
        bussinessStatisticsMap.put(Constants.METRICS_LOADER, new BussinessStatistics());

        //        sumStatisticsMap.put(Constants.METRICS_SUM_TRANS, new SumStatistics());
        //        sumStatisticsMap.put(Constants.METRICS_SUM_LOADER, new SumStatistics());

        return new KafkaBussinessListeners(sumStatisticsMap, bussinessStatisticsMap);
    }

    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return new DefaultKafkaConsumerFactory<String, String>(properties);
    }
}