package order;

import javax.persistence.Entity;
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
    @NonNull
    @Column
    private Long id;

    @Getter
    @NonNull
    @Column
    private String address;
}