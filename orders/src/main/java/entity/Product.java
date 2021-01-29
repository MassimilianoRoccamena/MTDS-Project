package entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = {"name", "manufacturer"}) }) 
public class Product {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;
    
    @NonNull
    @Column
    private String name;
    
    @NonNull
    @Column
    private String manufacturer;
}