package app;

import java.util.concurrent.ExecutionException;

public interface UserController
{
    void registerCustomer(String customerName, String customerAddress) throws InterruptedException, ExecutionException;
    void registerDeliveryMan(String deliveryManName) throws InterruptedException, ExecutionException;
}