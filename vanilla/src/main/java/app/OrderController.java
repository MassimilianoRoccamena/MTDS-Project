package app;

import java.util.List;
import java.util.concurrent.ExecutionException;

import app.data.Product;
import app.data.Order;

public interface OrderController
{
    void addProduct(Product product);
    void submitOrder(String customerName, List<Order.Field> orderFields) throws InterruptedException, ExecutionException;
}