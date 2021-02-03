package shipping;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@RequiredArgsConstructor
@Entity
public class Delivery {

    @Getter
    @Id
    @GeneratedValue
    @Column
    private Long id;
    
    @Getter
    @NonNull
    @Column(unique = true)
    private Long orderId;
    
    @Getter
    @NonNull
    @Column
    private Long deliveryManId;

    @Getter
    @NonNull
    @Column
    private String address;

    @Getter
    @Setter
    @Column
    private Boolean delivered = Boolean.FALSE;
}