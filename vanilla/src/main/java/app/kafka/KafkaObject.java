package app.kafka;

public abstract class KafkaObject
{
    protected String topic;

    public KafkaObject(String topic) {
        this.topic = topic;
    }
}