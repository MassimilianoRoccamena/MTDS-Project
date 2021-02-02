package order;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
public class Customer {
    
    @Id
    @NonNull
    @Column
    private Long id;

    @NonNull
    @Column
    private String address;
}