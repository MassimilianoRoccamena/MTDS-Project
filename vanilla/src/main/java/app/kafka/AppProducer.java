package app.kafka;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.*;

import app.kafka.KafkaConfig;
import app.kafka.KafkaObject;

public class AppProducer extends KafkaObject
{
    private KafkaProducer<String, String> producer;
    
    public AppProducer(String topic) {
        super(topic);
        Properties props = KafkaConfig.producerProperties();
        this.producer = new KafkaProducer<>(props);
    }

    public Future<RecordMetadata> produce(String key, String value) {
        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        return this.producer.send(record);
    }
}