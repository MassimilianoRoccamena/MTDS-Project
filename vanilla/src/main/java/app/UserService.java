package app;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.*;

import app.data.User;
import app.data.Customer;
import app.data.DeliveryMan;

import app.kafka.KafkaConfig;

public class UserService extends BasicService
{
    private Map<String, User> userData;
    private KafkaProducer<String, String> producer;
    private KafkaProducer<String, String> transactionalProducer;

    public UserService()
    {
        super();
        userData = new HashMap<>();
        producer = new KafkaProducer<>(KafkaConfig.producerProperties());
        transactionalProducer = new KafkaProducer<>(KafkaConfig.transactionalProducerProperties("User"));
        transactionalProducer.initTransactions();
    }

    public void registerCustomer(String customerName, String customerAddress) throws InterruptedException, ExecutionException
    {
        User user = userData.get(customerName);

        if (user != null)
        {
            if (user instanceof Customer)
            {
                // Name already used
            }
        }

        transactionalProducer.beginTransaction();
        user = new Customer(customerName, customerAddress);
        ProducerRecord<String, String> record;
        record = new ProducerRecord<>("NewCustomerName", customerName, customerName);
        transactionalProducer.send(record).get();
        String message = customerName + " " + customerAddress;
        record = new ProducerRecord<>("NewCustomerAddress", message, message);
        transactionalProducer.send(record).get();
        userData.put(customerName, user);
        transactionalProducer.commitTransaction();
    }
    
    public void registerDeliveryMan(String deliveryManName) throws InterruptedException, ExecutionException
    {
        User user = userData.get(deliveryManName);

        if (user != null)
        {
            if (user instanceof Customer)
            {
                // Name already used
            }
        }

        user = new DeliveryMan(deliveryManName);
        ProducerRecord<String, String> record;
        record = new ProducerRecord<>("NewDeliveryManName", deliveryManName, deliveryManName);
        producer.send(record).get();
        userData.put(deliveryManName, user);
    }

    public void doService()
    {
        //CLI

        producer.close();
        transactionalProducer.close();
    }

    public static void main( String[] args ) 
    {
        BasicService service = new UserService();
        service.doService();
    }
}