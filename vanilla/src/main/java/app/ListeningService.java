package app;

import java.util.List;
import java.util.ArrayList;

import app.kafka.KafkaListener;

public abstract class ListeningService extends BasicService {

    private List<KafkaListener> kafkaListeners;

    public ListeningService()
    {
        kafkaListeners = new ArrayList<>();
    }

    public void addListener(KafkaListener listener)
    {
        kafkaListeners.add(listener);
    }

    public void doService()
    {
        for (Runnable listener : kafkaListeners)
        {
            new Thread(listener).start();
        }
    }
}