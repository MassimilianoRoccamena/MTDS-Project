package app;

public class TopicBuilder
{
    public static String createTopic(String from, String to, String name)
    {
        return from + ":" + to + "-" + name;
    }
}