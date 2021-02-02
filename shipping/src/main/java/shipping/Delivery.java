package shipping;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
public class Delivery {

    @Id
    @GeneratedValue
    @Column
    private Long id;
    
    @NonNull
    @Column(unique = true)
    private Long orderId;
    
    @NonNull
    @Column
    private Long deliveryManId;

    @NonNull
    @Column
    private String address;

    @Column
    private Boolean delivered = Boolean.FALSE;
}