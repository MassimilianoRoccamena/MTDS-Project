package app.data;

import java.util.List;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import lombok.Getter;

@Getter
public class Order
{   
    @RequiredArgsConstructor
    @Getter
    public class Field
    {   
        @NonNull
        Product product;
        @NonNull
        Integer count;
    }

    private static Integer currentId = 0;

    @NonNull
    private Integer id;
    @NonNull
    private String customer;
    
    private List<Field> fields;

    public Order(String customer) {
        synchronized (currentId)
        {
            this.id = currentId;
            currentId += 1;
        }

        this.customer = customer;
        fields = new ArrayList<>();
    }
}