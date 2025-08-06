package boot.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "industries")
@Entity
public class Industry {
    
    @Id
    private Long id;

    
    private String name;
}
