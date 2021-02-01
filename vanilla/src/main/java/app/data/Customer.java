package app.data;

import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import lombok.Getter;

import app.data.User;

@RequiredArgsConstructor
public class Customer extends User
{
    @NonNull
    @Getter
    private String address;
}