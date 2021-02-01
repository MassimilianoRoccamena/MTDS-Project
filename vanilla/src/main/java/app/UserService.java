package app;

import java.util.Map;
import java.util.HashMap;

import org.apache.kafka.clients.consumer.ConsumerRecords;

import app.data.User;
import app.data.Customer;
import app.data.DeliveryMan;
import app.kafka.AppProducer;
import app.kafka.AppConsumer;

import app.BasicService;
import static app.TopicBuilder.createTopic;

public class UserService extends BasicService
{
    private class IsValidCustomerConsumer extends AppConsumer
    {
        public IsValidCustomerConsumer(String topic)
        {
            super(topic);
        }

        public void consume(ConsumerRecords<String, String> records)
        {
            
        }
    }

    private class IsValidDeliveryManConsumer extends AppConsumer
    {
        public IsValidDeliveryManConsumer(String topic)
        {
            super(topic);
        }

        public void consume(ConsumerRecords<String, String> records)
        {
            
        }
    }

    private class GetServiceAddressConsumer extends AppConsumer
    {
        public GetServiceAddressConsumer(String topic)
        {
            super(topic);
        }

        public void consume(ConsumerRecords<String, String> records)
        {
            
        }
    }
    

    private AppConsumer isValidCustomerConsumer;
    private AppProducer isValidCustomerProducer;

    private AppConsumer isValidDeliveryManConsumer;
    private AppProducer isValidDeliveryManProducer;

    private AppConsumer getCustomerAddressConsumer;
    private AppProducer getCustomerAddressProducer;

    private Map<String, User> userData;

    public UserService()
    {
        super();

        String functionName = "isValidCustomer";
        isValidCustomerConsumer = new IsValidCustomerConsumer(createTopic("order", "user", functionName));
        isValidCustomerProducer = new AppProducer(createTopic("user", "order", functionName));
        functionName = "isValidDeliveryMan";
        isValidDeliveryManConsumer = new IsValidDeliveryManConsumer(createTopic("shipping", "user", functionName));
        isValidCustomerProducer = new AppProducer(createTopic("user", "shipping", functionName));
        functionName = "getCustomerAddress";
        getCustomerAddressConsumer = new GetServiceAddressConsumer(createTopic("shipping", "user", functionName));
        getCustomerAddressProducer = new AppProducer(createTopic("user", "shipping", functionName));

        addListener(isValidCustomerConsumer);
        addListener(isValidDeliveryManConsumer);
        addListener(getCustomerAddressConsumer);

        userData = new HashMap<String, User>();
    }

    public String getServiceAddress()
    {
        return "localhost:8081";
    }

    public void doService()
    {
        startListening();
    }

    public static void main( String[] args ) 
    {
        BasicService service = new UserService();
        service.doService();
    }
}