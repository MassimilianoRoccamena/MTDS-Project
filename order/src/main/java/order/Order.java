package order;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "customer_order")
public class Order {
    
    @NoArgsConstructor
    @Getter
    @Setter
    @Embeddable
    public static class Field {

        @NonNull
        private String name;

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

    @Getter
    @Setter
    @Column
    private Boolean delivered = Boolean.FALSE;
}