package entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Data;

import dto.LoginForm;

@Data
@Entity
public class Customer {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;
    
    @Column(unique = true)
    private String name;
 
    @Column
    private String address;

    public Customer(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public static Customer fromDTO(LoginForm loginForm) {
        return new Customer(loginForm.getName(), loginForm.getAddress());
    }
}