package app;

import java.util.List;
import java.util.ArrayList;

import app.kafka.AppConsumer;

public abstract class BasicService {

    private List<AppConsumer> consumers;

    public BasicService()
    {
        consumers = new ArrayList<>();
    }

    public void addListener(AppConsumer consumer)
    {
        consumers.add(consumer);
    }
    public void startListening()
    {
        for (AppConsumer consumer : consumers)
        {
            consumer.run();
        }
    }

    public abstract String getServiceAddress();

    public abstract void doService();
}