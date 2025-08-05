package boot.data.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "final")
@Entity
public class IndustriesDto {
    
    @Id
    private long id;

    
    private String name;
}
