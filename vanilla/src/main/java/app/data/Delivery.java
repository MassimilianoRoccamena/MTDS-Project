package app.data;

import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import lombok.Getter;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class Delivery
{
    @NonNull
    private Integer orderId;
    @NonNull
    private String deliveryMan;
    @NonNull
    private String address;

    @Setter
    private Boolean delivered = Boolean.FALSE;
}