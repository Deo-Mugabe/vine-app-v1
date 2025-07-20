package vine.vine.domain.dto.response;

import lombok.Data;
import vine.vine.domain.Charges;

@Data
public class ChargesResponse {
    private Long archrgid;
    private Long bookId;
    private String arr_chrg;
    private String fel_misd;
    private String chrg_cnt;
    private String chrg_seq;
    private String bondamt;
    private String bondtype;
    private Long armainid;
    private String chrgdesc;

    public static ChargesResponse from(Charges charges) {
        ChargesResponse response = new ChargesResponse();
        response.setArchrgid(charges.getArchrgid());
        response.setBookId(charges.getBookId());
        response.setArr_chrg(charges.getArr_chrg());
        response.setFel_misd(charges.getFel_misd());
        response.setChrg_cnt(charges.getChrg_cnt());
        response.setChrg_seq(charges.getChrg_seq());
        response.setBondamt(charges.getBondamt());
        response.setBondtype(charges.getBondtype());
        response.setArmainid(charges.getArmainid());
        response.setChrgdesc(charges.getChrgdesc());

        return response;
    }
}

