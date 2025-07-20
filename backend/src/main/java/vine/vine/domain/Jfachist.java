package vine.vine.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@RequiredArgsConstructor
public class Jfachist {

    @Id
    @Column(name = "jfachistid", nullable = false)
    private Long jfachistId;  // bigint -> Long

    @Column(name = "book_id")
    private Long bookId;      // bigint -> Long

    @Column(name = "facility")
    private String facility;

    @Column(name = "section")
    private String section;

    @Column(name = "unit")
    private String unit;

    @Column(name = "bed")
    private String bed;

    @Column(name = "eventdate")
    private LocalDateTime eventDate;  // timestamp -> LocalDateTime

    // Getters and setters omitted for brevity
}