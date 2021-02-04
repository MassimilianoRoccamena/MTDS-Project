package user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Entity
public class DeliveryMan {
    
    @Id
    @GeneratedValue
    @Column
    private Long id;
    
    @NonNull
    @Column(unique = true)
    private String name;
}
