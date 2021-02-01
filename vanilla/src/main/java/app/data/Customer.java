package app.data;

import lombok.NonNull;
import lombok.Getter;

import app.data.User;

@Getter
public class Customer extends User
{
    @NonNull
    private String address;

    public Customer(String name, String address)
    {
        this.name = name;
        this.address = address;
    }
}