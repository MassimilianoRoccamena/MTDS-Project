package app.kafka;

import java.util.Collections;
import java.util.Properties;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import app.kafka.KafkaConfig;
import app.kafka.KafkaObject;

public abstract class AppConsumer extends KafkaObject implements Runnable
{
    
    private KafkaConsumer<String, String> consumer;
    
    public AppConsumer(String topic) {
        super(topic);
        Properties props = KafkaConfig.consumerProperties();
        this.consumer = new KafkaConsumer<>(props);
    }

    @Override
    public void run() {
        this.consumer.subscribe(Collections.singletonList(topic));

        while (true) {
            final ConsumerRecords<String, String> records = this.consumer.poll(Duration.of(3, ChronoUnit.SECONDS));
            this.consume(records);
        }
    }

    public abstract void consume(ConsumerRecords<String, String> records);
}