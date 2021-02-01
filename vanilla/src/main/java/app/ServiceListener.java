package app;

import org.apache.kafka.clients.consumer.*;

public interface ServiceListener extends Runnable
{
    public void consume(ConsumerRecords<String, String> records);
}
