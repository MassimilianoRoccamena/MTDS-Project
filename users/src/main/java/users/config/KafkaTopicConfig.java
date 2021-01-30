package users.config;

import java.util.Map;
import java.util.HashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.kafka.core.KafkaAdmin;
import org.apache.kafka.clients.admin.AdminClientConfig;
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
    public NewTopic topic11() {
         return new NewTopic("orders:users:isValidCustomer", 1, (short) 1);
    }
    @Bean
    public NewTopic topic12() {
         return new NewTopic("users:orders:isValidCustomer", 1, (short) 1);
    }
    @Bean
    public NewTopic topic21() {
         return new NewTopic("shipping:users:isValidDeliveryMan", 1, (short) 1);
    }
    @Bean
    public NewTopic topic22() {
         return new NewTopic("users:shipping:isValidDeliveryMan", 1, (short) 1);
    }
    @Bean
    public NewTopic topic31() {
         return new NewTopic("orders:users:getCustomerAddress", 1, (short) 1);
    }
    @Bean
    public NewTopic topic32() {
         return new NewTopic("users:orders:getCustomerAddress", 1, (short) 1);
    }
    @Bean
    public NewTopic topic41() {
         return new NewTopic("shipping:users:getAvailableDeliveryMan", 1, (short) 1);
    }
    @Bean
    public NewTopic topic42() {
         return new NewTopic("users:shipping:getAvailableDeliveryMan", 1, (short) 1);
    }
}