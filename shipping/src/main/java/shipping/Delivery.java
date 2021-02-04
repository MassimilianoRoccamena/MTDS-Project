package shipping;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Entity
public class Delivery {
    
    @Id
    @NonNull
    @Column
    private Long orderId;
    
    @NonNull
    @Column
    private Long deliveryManId;

    @NonNull
    @Column
    private String address;

    @Setter
    @Column
    private Boolean delivered = Boolean.FALSE;
}