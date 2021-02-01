package app.data;

import lombok.Data;
import lombok.NonNull;

import app.data.User;

@Data
public class Customer extends User
{
    @NonNull
    private String address;
}