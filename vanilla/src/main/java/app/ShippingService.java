package app;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.kafka.clients.consumer.*;

import app.data.Delivery;

public class ShippingService extends ListeningService
{
    public class NewDeliveryManNameListener extends KafkaListener<ShippingService> 
    {
        public NewDeliveryManNameListener(ShippingService parentService)
        {
            super(parentService, KafkaConfig.consumerProperties(parentService.getServiceName()), "NewDeliveryManName");
        }

        @Override
        public void consume(ConsumerRecords<String, String> records)
        {
            // TO DO
        }
    }

    public class NewCustomerAddressListener extends KafkaListener<ShippingService> 
    {
        public NewCustomerAddressListener(ShippingService parentService) {
            super(parentService, KafkaConfig.transactionalConsumerProperties(parentService.getServiceName()), "NewCustomerAddress");
        }

        @Override
        public void consume(ConsumerRecords<String, String> records) {
            // TO DO
        }
    }

    public class NewOrderListener extends KafkaListener<ShippingService> 
    {
        public NewOrderListener(ShippingService parentService) {
            super(parentService, KafkaConfig.transactionalConsumerProperties(parentService.getServiceName()), "NewOrder");
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

    public String getServiceName()
    {
        return "Shipping";
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