package app;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;

import app.data.Product;
import app.data.Order;

public class OrderService extends ListeningService implements OrderController
{
    public class NewCustomerNameListener extends KafkaListener<OrderService> 
    {
        public NewCustomerNameListener(OrderService parentService) {
            super(parentService, KafkaConfig.transactionalConsumerProperties(parentService.getServiceName()), "NewCustomerName");
        }

        @Override
        public void consume(ConsumerRecords<String, String> records) {
            // TO DO
        }
    }

    private List<String> customerNameData;
    private List<Product> productData;
    private Map<String, Order> orderData;

    private KafkaProducer<String, String> producer;

    public OrderService()
    {
        super();
        customerNameData = new ArrayList<>();
        productData = new ArrayList<>();
        orderData = new HashMap<>();
        producer = new KafkaProducer<>(KafkaConfig.producerProperties());
        addListener(new NewCustomerNameListener(this));
    }

    public void addProduct(Product product)
    {
        if (productData.contains(product))
        {
            // Product already added
        }

        productData.add(product);
    }

    public void submitOrder(String customerName, List<Order.Field> orderFields) throws InterruptedException, ExecutionException
    {
        if (!customerNameData.contains(customerName))
        {
            // Invalid user
        }

        for (Order.Field field : orderFields)
        {
            if (!productData.contains(field.getProduct()))
            {
                // Invalid product
            }
        }

        Order order = new Order(customerName);
        order.getFields().addAll(orderFields);
        String message = customerName + " " + order.getId();
        ProducerRecord<String, String> record = new ProducerRecord<>("NewOrder", message, message);
        producer.send(record).get();
        orderData.put(customerName, order);
    }

    public String getServiceName()
    {
        return "Order";
    }

    public void doService()
    {
        super.doService();

        //CLI

        producer.close();
    }

    public static void main( String[] args ) 
    {
        BasicService service = new OrderService();
        service.doService();
    }
}