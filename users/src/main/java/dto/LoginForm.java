package dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class LoginForm {

    @NonNull
    private String name;

    @NonNull
    private String address;
}