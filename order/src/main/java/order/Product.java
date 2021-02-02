package order;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
public class Product {

    @Id
    @GeneratedValue
    @Column
    private Long id;
    
    @NonNull
    @Column
    private String name;
}