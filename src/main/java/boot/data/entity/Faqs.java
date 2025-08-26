package boot.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.sql.Timestamp;

@Entity
@Table(name = "faqs") // DB 테이블 이름 'faqs'와 일치
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Faqs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // DB 컬럼명 'id'와 일치
    private Long id;

    @Column(name = "category", length = 50) // DB 컬럼명 'category'와 길이 일치
    private String category;

    @Column(name = "question", length = 255) // DB 컬럼명 'question'과 길이 일치
    private String question;

    @Column(name = "answer", columnDefinition = "TEXT") // DB 컬럼명 'answer'와 타입 일치
    private String answer;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false) // DB 컬럼명 'created_at'과 일치
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at") // DB 컬럼명 'updated_at'과 일치
    private Timestamp updatedAt;
}
