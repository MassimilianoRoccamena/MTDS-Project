package app;

import java.util.Properties;
import java.util.Collections;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import lombok.Getter;

import org.apache.kafka.clients.consumer.*;

public abstract class KafkaListener<S extends BasicService> implements ServiceListener
{
    @Getter
    private S parentService;
    private KafkaConsumer<String, String> consumer;
    
    public KafkaListener(S parentService, Properties properties, String topic) {
        this.parentService = parentService;
        this.consumer = new KafkaConsumer<>(properties);
        this.consumer.subscribe(Collections.singletonList(topic));
    }

    @Override
    public void run() {
        while (true) {
            final ConsumerRecords<String, String> records = this.consumer.poll(Duration.of(3, ChronoUnit.SECONDS));
            this.consume(records);
        }
    }

    public abstract void consume(ConsumerRecords<String, String> records);
}