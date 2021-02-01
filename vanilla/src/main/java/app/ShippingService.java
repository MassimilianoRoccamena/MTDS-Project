package app;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.kafka.clients.consumer.*;

import app.data.Delivery;

import app.kafka.KafkaConfig;
import app.kafka.KafkaListener;

public class ShippingService extends ListeningService
{
    public class NewDeliveryManNameListener extends KafkaListener 
    {
        public NewDeliveryManNameListener()
        {
            super(KafkaConfig.consumerProperties("Shipping"), "NewDeliveryManName");
        }

        @Override
        public void consume(ConsumerRecords<String, String> records)
        {
            // TO DO
        }
    }

    public class NewCustomerAddressListener extends KafkaListener 
    {
        public NewCustomerAddressListener(KafkaConsumer<String, String> consumer) {
            super(KafkaConfig.transactionalConsumerProperties("Shipping"), "NewCustomerAddress");
        }

        @Override
        public void consume(ConsumerRecords<String, String> records) {
            // TO DO
        }
    }

    public class NewOrderListener extends KafkaListener 
    {
        public NewOrderListener(KafkaConsumer<String, String> consumer) {
            super(KafkaConfig.transactionalConsumerProperties("Shipping"), "NewOrder");
        }

        @Override
        public void consume(ConsumerRecords<String, String> records) {
            // TO DO
        }
    }

    private List<String> deliveryManNameData;
    private Map<String, String> customerAddressData;
    private Map<String, List<Delivery>> deliveryData;

    public ShippingService()
    {
        super();
        deliveryManNameData = new ArrayList<>();
        customerAddressData = new HashMap<>();
        deliveryData = new HashMap<>();
    }

    public void notifyDelivery(String deliveryManName, Integer orderId)
    {
        List <Delivery> deliveries = deliveryData.get(deliveryManName);

        if (deliveries == null)
        {
            // Invalid delivery man
        }

        Delivery requestedDelivery = null;

        for (Delivery delivery : deliveryData.get(deliveryManName))
        {
            if (delivery.getOrderId().equals(orderId))
            {
                requestedDelivery = delivery;
                break;
            }
        }

        if (requestedDelivery == null)
        {
            // Invalid order
        }

        requestedDelivery.setDelivered(Boolean.TRUE);
    }

    public void doService()
    {
        super.doService();

        //CLI
    }

    public static void main( String[] args ) 
    {
        BasicService service = new ShippingService();
        service.doService();
    }
}