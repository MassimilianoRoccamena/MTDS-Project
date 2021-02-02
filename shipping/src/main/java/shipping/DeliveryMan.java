package shipping;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
public class DeliveryMan {
    
    @Id
    @NonNull
    @Column
    private Long id;
}
