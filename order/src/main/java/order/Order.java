package order;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
public class Order {
    
    @Data
    @Embeddable
    public class Field {

        @NonNull
        private String productName;

        @NonNull
        private Integer count;
    }

    @Id
    @GeneratedValue
    @Column
    private Long id;
    
    @NonNull
    @Column
    private Long customerId;
    
    @NonNull
    @ElementCollection
    @Column
    private List<Field> fields;
}