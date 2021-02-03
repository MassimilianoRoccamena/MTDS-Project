package shipping;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@RequiredArgsConstructor
@Entity
public class DeliveryMan {
    
    @Getter
    @Id
    @NonNull
    @Column
    private Long id;
}
