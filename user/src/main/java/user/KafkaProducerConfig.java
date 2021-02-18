package user;

import java.util.Map;
import java.util.HashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

@Configuration
public class KafkaProducerConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public Map<String, Object> basicProperties() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
          ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
          bootstrapAddress);
        configProps.put(
          ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
          StringSerializer.class);
        configProps.put(
          ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
          StringSerializer.class);
        configProps.put(
          ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
          true);
        return configProps;
    }

    @Bean
    public ProducerFactory<String, String> newCustomeryManProducerFactory(Map<String, Object> basicProperties) {
        Map<String, Object> configProps = new HashMap<>(basicProperties);
        configProps.put(
          ProducerConfig.TRANSACTIONAL_ID_CONFIG,
          "NewCustomerMan");
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    @Bean
    public ProducerFactory<String, String> newDeliveryManProducerFactory(Map<String, Object> basicProperties) {
        Map<String, Object> configProps = new HashMap<>(basicProperties);
        configProps.put(
          ProducerConfig.TRANSACTIONAL_ID_CONFIG,
          "NewDeliveryMan");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> newCustomerKafkaTemplate(ProducerFactory<String, String> newCustomerProducerFactory) {
        return new KafkaTemplate<>(newCustomerProducerFactory);
    }
    @Bean
    public KafkaTemplate<String, String> newDeliveryManKafkaTemplate(ProducerFactory<String, String> newDeliveryManProducerFactory) {
        return new KafkaTemplate<>(newDeliveryManProducerFactory);
    }
}