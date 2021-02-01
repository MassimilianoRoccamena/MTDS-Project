package app.data;

import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@Getter
@Setter
public abstract class User
{
    @NonNull
    private String name;
}