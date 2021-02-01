package app;

import java.util.Map;
import java.util.HashMap;

import org.apache.kafka.clients.producer.*;

import app.data.User;
import app.data.Customer;
import app.data.DeliveryMan;

import app.BasicService;
import app.kafka.KafkaConfig;

public class UserService extends BasicService
{
    private Map<String, User> userData;
    private KafkaProducer<String, String> producer;
    private KafkaProducer<String, String> transactionalProducer;

    public UserService()
    {
        super();
        userData = new HashMap<String, User>();
        producer = new KafkaProducer<>(KafkaConfig.producerProperties());
        transactionalProducer = new KafkaProducer<>(KafkaConfig.transactionalProducerProperties("User"));
        transactionalProducer.initTransactions();
    }

    public void registerCustomer(String name, String address)
    {
        User user = new Customer(name, address);
        ProducerRecord<String, String> record;

        transactionalProducer.beginTransaction();
        record = new ProducerRecord<>("CustomerName", name, name);
        transactionalProducer.send(record).get();
        record = new ProducerRecord<>("CustomerAddress", address, address);
        transactionalProducer.send(record).get();
        transactionalProducer.commitTransaction();

        userData.put(name, user);
    }
    public void registerDeliveryMan(String name)
    {
        User user = new DeliveryMan(name);
        ProducerRecord<String, String> record;
        record = new ProducerRecord<>("DeliveryManName", name, name);
        producer.send(record).get();
        userData.put(name, user);
    }

    public void doService()
    {
        //CLI

        producer.close();
    }

    public static void main( String[] args ) 
    {
        BasicService service = new UserService();
        service.doService();
    }
}