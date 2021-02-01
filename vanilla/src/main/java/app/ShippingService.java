package app;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import lombok.Getter;

import org.apache.kafka.clients.consumer.*;

import app.data.Delivery;

public class ShippingService extends ListeningService implements ShippingController
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
            for (ConsumerRecord<String, String> record : records)
            {
                String deliveryManName = record.value();
                parentService.getDeliveryManNameData().add(deliveryManName);
                parentService.deliveryData.put(deliveryManName, new ArrayList<>());
            }
        }
    }

    public class NewCustomerAddressListener extends KafkaListener<ShippingService> 
    {
        public NewCustomerAddressListener(ShippingService parentService)
        {
            super(parentService, KafkaConfig.transactionalConsumerProperties(parentService.getServiceName()), "NewCustomerAddress");
        }

        @Override
        public void consume(ConsumerRecords<String, String> records)
        {
            for (ConsumerRecord<String, String> record : records)
            {
                String message = record.value();
                String[] splittedMessage = message.split(" ");
                String customerName = splittedMessage[0];
                String customerAddress = splittedMessage[1];
                parentService.getCustomerAddressData().put(customerName, customerAddress);
            }
        }
    }

    public class NewOrderListener extends KafkaListener<ShippingService> 
    {
        public NewOrderListener(ShippingService parentService)
        {
            super(parentService, KafkaConfig.transactionalConsumerProperties(parentService.getServiceName()), "NewOrder");
        }

        @Override
        public void consume(ConsumerRecords<String, String> records)
        {
            for (ConsumerRecord<String, String> record : records)
            {
                String message = record.value();
                String[] splittedMessage = message.split(" ");
                String customerName = splittedMessage[0];
                Integer orderId = Integer.parseInt(splittedMessage[1]);
                String address = parentService.customerAddressData.get(customerName);
                String deliveryManName = parentService.deliveryManNameData.get(0);
                Delivery delivery = new Delivery(orderId, deliveryManName, address);
                List <Delivery> userDeliveries = parentService.deliveryData.get(deliveryManName);
                userDeliveries.add(delivery);
            }
        }
    }

    @Getter
    private List<String> deliveryManNameData;
    @Getter
    private Map<String, String> customerAddressData;
    @Getter
    private Map<String, List<Delivery>> deliveryData;

    public ShippingService()
    {
        super();
        deliveryManNameData = new ArrayList<>();
        customerAddressData = new HashMap<>();
        deliveryData = new HashMap<>();
        addListener(new NewDeliveryManNameListener(this));
        addListener(new NewCustomerAddressListener(this));
        addListener(new NewOrderListener(this));
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