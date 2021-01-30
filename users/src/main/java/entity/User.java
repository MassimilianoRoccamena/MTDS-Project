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
public class User {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;
    
    @NonNull
    @Column
    private Boolean customer;
    
    @NonNull
    @Column(unique = true)
    private String name;

    @NonNull
    @Column
    private String address;

    public static User newCustomer(LoginForm loginForm) {
        return new User(Boolean.TRUE, loginForm.getName(), loginForm.getAddress());
    }
    public static User newCustomer(String name, String address) {
        return new User(Boolean.TRUE, name, address);
    }
    public static User newDeliveryMan(String name) {
        return new User(Boolean.TRUE, name, null);
    }
}