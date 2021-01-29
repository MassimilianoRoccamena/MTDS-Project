package entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Data;
import lombok.NonNull;

import dto.LoginForm;

@Data
@Entity
public class Customer {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;
    
    @NonNull
    @Column(unique = true)
    private String name;
    
    @NonNull
    @Column
    private String address;

    public static Customer fromDTO(LoginForm loginForm) {
        return new Customer(loginForm.getName(), loginForm.getAddress());
    }
}