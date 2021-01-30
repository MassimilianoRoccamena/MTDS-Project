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
    
    @Column
    private Boolean customer = Boolean.TRUE;
    
    @NonNull
    @Column(unique = true)
    private String name;

    @NonNull
    @Column
    private String address;

    public static User newCustomer(LoginForm loginForm) {
        return new User(loginForm.getName(), loginForm.getAddress());
    }
}