package app.kafka;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.apache.kafka.clients.consumer.*;

public abstract class KafkaListener implements Runnable
{
    
    private KafkaConsumer<String, String> consumer;
    
    public KafkaListener(KafkaConsumer<String, String> consumer) {
        this.consumer = consumer;
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