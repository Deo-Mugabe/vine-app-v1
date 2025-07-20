package vine.vine.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Data
@RequiredArgsConstructor
@Table(name = "jmmain")
public class Jmmain {

    @Id
    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "bookdate", nullable = false)
    private LocalDateTime bookDate;

    @Column(name = "agency", nullable = false)
    private String agency;

    @Column(name = "addtime", nullable = false)
    private LocalDateTime addTime;

    @Column(name = "name_id", nullable = false)
    private Long nameId;

    @Column(name = "bkstatus")
    private String bkStatus;

    @Column(name = "faci_id")
    private Long faciId;

    // Getters and setters omitted for brevity
}