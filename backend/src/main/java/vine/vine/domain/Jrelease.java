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
public class Jrelease {

    @Id
    @Column(name = "jreleaseid")
    private Integer jreleaseId;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "releasetime")
    private LocalDateTime releasetime;

    @Column(name = "relsreason")
    private String relsreason;
}
