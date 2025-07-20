package vine.vine.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "armain")
public class Armain {
    @Id
    @Column(name = "armainid")
    private Long armainid;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name="case_id")
    private String caseId;

    @Column(name="date_arr")
    private LocalDateTime dateArrest;
}
