package user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
public class Customer {

    @Id
    @GeneratedValue
    @Column
    private Long id;
    
    @NonNull
    @Column(unique = true)
    private String name;

    @NonNull
    @Column
    private String address;
}