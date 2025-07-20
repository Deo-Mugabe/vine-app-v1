package vine.vine.domain.dto.response;
import java.time.LocalDateTime;

import lombok.Data;
import vine.vine.domain.Armain;

@Data
public class ArmainResponse {
    private Long armainid;
    private Long bookId;
    private String caseId;
    private LocalDateTime dateArrest;

    public static ArmainResponse from(Armain armain){
        ArmainResponse armainResponse = new ArmainResponse();
        armainResponse.armainid = armain.getArmainid();
        armainResponse.bookId = armain.getBookId();
        armainResponse.caseId = armain.getCaseId();
        armainResponse.dateArrest = armain.getDateArrest();
        return armainResponse;
    }
}
