package entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
public class DeliveryMan {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;
    
    @NonNull
    @Column(unique = true)
    private String name;
}