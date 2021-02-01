package app;

public abstract class BasicService
{
    public abstract String getServiceName();
    public abstract void doService();

    public void printLog(String body)
    {
        System.out.println("[LOG] " + body);
    }
}