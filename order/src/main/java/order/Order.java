package order;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@RequiredArgsConstructor
@Entity
public class Order {
    
    @NoArgsConstructor
    @RequiredArgsConstructor
    @Embeddable
    public class Field {

        @Getter
        @NonNull
        private String name;

        @Getter
        @NonNull
        private Integer count;
    }

    @Getter
    @Id
    @GeneratedValue
    @Column
    private Long id;
    
    @Getter
    @NonNull
    @Column
    private Long customerId;
    
    @Getter
    @NonNull
    @ElementCollection
    @Column
    private List<Field> fields;
}