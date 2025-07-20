package vine.vine.domain.dto.response;

import lombok.Data;
import vine.vine.domain.Systab1;

@Data
public class Systab1Response {
    private Integer systab1id;
    private String codeAgcy;
    private String codeKey;
    private String sys_msg;

    public static Systab1Response from(Systab1 systab1){
        Systab1Response response = new Systab1Response();
        response.systab1id = systab1.getSystab1id();
        response.codeAgcy = systab1.getCodeAgcy();
        response.codeKey = systab1.getCodeKey();
        response.sys_msg = systab1.getSys_msg();

        return response;
    }
}
