package entity;

import lombok.Data;
import lombok.NonNull;

@Data
public class OrderField {

    @NonNull
    private Long productId;

    @NonNull
    private Integer count;
}