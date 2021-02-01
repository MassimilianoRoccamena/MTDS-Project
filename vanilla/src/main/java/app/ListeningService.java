package app;

import java.util.List;
import java.util.ArrayList;

public abstract class ListeningService extends BasicService
{
    private List<ServiceListener> kafkaListeners;

    public ListeningService()
    {
        kafkaListeners = new ArrayList<>();
    }

    public void addListener(ServiceListener listener)
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