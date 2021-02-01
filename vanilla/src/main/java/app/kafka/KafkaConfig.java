package app.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaConfig
{
    private static Properties consumerProperties;
    private static Properties producerProperties;

    public static final String address = "localhost:9092";

    public static Properties consumerProperties()
    {
        if (consumerProperties == null)
        {

        }

        return consumerProperties;
    }
    public static Properties producerProperties()
    {
        if (producerProperties == null)
        {
            producerProperties = new Properties();
            producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, address);
            producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        }

        return producerProperties;
    }
}