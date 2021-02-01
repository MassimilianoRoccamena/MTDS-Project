package app.data;

import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import lombok.Getter;

@RequiredArgsConstructor
@Getter
public class Product
{
    @NonNull
    private String name;
}
