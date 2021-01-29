package entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;
    
    @NonNull
    @Column
    private Long customerId;
    
    @NonNull
    @Column
    private List<OrderField> fields;
}