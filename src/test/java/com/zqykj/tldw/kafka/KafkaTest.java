package com.zqykj.tldw.kafka;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by weifeng on 2018/5/7.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaTest {

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Test
    public void testKafkaSend(){
        kafkaTemplate.send("test", "key111", "value111");

    }

    @Test
    public void testKakfaConsumer(){
        kafkaTemplate.getMessageConverter();

    }

}
