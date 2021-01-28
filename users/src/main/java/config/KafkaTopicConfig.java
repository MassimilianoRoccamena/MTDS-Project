package config;

import java.util.Map;
import java.util.HashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Value;
import org.springframework.context.annotation.Configuration;

import org.apache.kafka.clients.consumer.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;

@Configuration
public class KafkaTopicConfig {
    
    @Value(value = "${kafka.bootstrapAddress}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }
    
    @Bean
    public NewTopic topic1() {
         return new NewTopic("isCustomerRegistered", 1, (short) 1);
    }
    @Bean
    public NewTopic topic2() {
         return new NewTopic("getCustomerAddress", 1, (short) 1);
    }
}