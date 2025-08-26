package boot.data.entity;

import jakarta.persistence.*; 
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "faqs")
@Getter
@Setter
public class Faqs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;
}