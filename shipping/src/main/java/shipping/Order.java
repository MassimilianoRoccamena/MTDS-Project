package shipping;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
@Table(name = "customer_order")
public class Order {
    
    @Id
    @NonNull
    @Column
    private Long id;
    
    @NonNull
    @Column
    private Long deliveryManId;

    @NonNull
    @Column
    private String customerAddress;

    @Setter
    @Column
    private Boolean delivered = Boolean.FALSE;
}