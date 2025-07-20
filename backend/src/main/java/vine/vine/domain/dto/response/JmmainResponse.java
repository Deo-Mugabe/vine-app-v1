package vine.vine.domain.dto.response;

import jakarta.persistence.Column;
import lombok.Data;
import vine.vine.domain.Jmmain;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class JmmainResponse {
    private Long bookId;
    private LocalDateTime bookDate;
    private String agency;
    private LocalDateTime addTime;
    private Long nameId;
    private String bkStatus;
    private Long faciId;


    public static JmmainResponse from(Jmmain jmmain){
        JmmainResponse response = new JmmainResponse();
        response.setNameId(jmmain.getNameId());
        response.setBookId(jmmain.getBookId());
        response.setAgency(jmmain.getAgency());
        response.setAddTime(jmmain.getAddTime());
        response.setBookDate(jmmain.getBookDate());
        response.setBkStatus(jmmain.getBkStatus());
        response.setFaciId(jmmain.getFaciId());

        return response;
    }
}