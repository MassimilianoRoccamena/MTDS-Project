package user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@RequiredArgsConstructor
@Entity
public class Customer {

    @Getter
    @Id
    @GeneratedValue
    @Column
    private Long id;
    
    @Getter
    @NonNull
    @Column(unique = true)
    private String name;

    @Getter
    @NonNull
    @Column
    private String address;
}