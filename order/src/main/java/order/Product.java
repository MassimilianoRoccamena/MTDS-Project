package order;

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
public class Product {

    @Getter
    @Id
    @GeneratedValue
    @Column
    private Long id;
    
    @Getter
    @NonNull
    @Column
    private String name;
}