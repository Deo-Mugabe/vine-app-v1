package vine.vine.domain.dto.response;

import jakarta.persistence.Column;
import lombok.Data;
import vine.vine.domain.Jrelease;

import java.time.LocalDateTime;

@Data
public class JreleaseResponse {

    private Integer jreleaseId;
    private Long bookId;
    private LocalDateTime releasetime;
    private String relsreason;

    public static JreleaseResponse from(Jrelease jrelease) {
        JreleaseResponse response = new JreleaseResponse();
        response.jreleaseId = jrelease.getJreleaseId();
        response.bookId = jrelease.getBookId();
        response.releasetime = jrelease.getReleasetime();
        response.relsreason = jrelease.getRelsreason();
        return response;
    }
}
