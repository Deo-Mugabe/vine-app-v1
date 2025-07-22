package vine.vine.domain.dto.response;

import java.time.LocalDate;

import lombok.Data;
import vine.vine.domain.Nmmain;


@Data
public class NmmainResponse {

    private Long nameId;
    private String stateId;
    private String nameType;
    private String aliasId;
    private String firstname;
    private String middlename;
    private String lastname;
    private String dob;
    private String race;
    private String sex;
    private String height;
    private String weight;
    private String ssn;
    private String streetNbr;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String birthplace;
    private String drLic;
    private String dlState;
    private String marital;
    private String occupation;
    private String eye;
    private String hair;
    private String employer;
    private String hphone;
    private String wphone;
    private String mphone;

    public static NmmainResponse from(Nmmain nmmain){
        NmmainResponse response = new NmmainResponse();
        response.setStateId(nmmain.getStateId());
        response.setNameType(nmmain.getNameType());
        response.setAliasId(nmmain.getAliasId());
        response.setNameId(nmmain.getNameId());
        response.setFirstname(nmmain.getFirstname());
        response.setMiddlename(nmmain.getMiddlename());
        response.setLastname(nmmain.getLastname());
        response.setDob(nmmain.getDob());
        response.setRace(nmmain.getRace());
        response.setSex(nmmain.getSex());
        response.setHeight(nmmain.getHeight());
        response.setWeight(nmmain.getWeight());
        response.setSsn(nmmain.getSsn());
        response.setStreet(nmmain.getStreet());
        response.setCity(nmmain.getCity());
        response.setState(nmmain.getState());
        response.setZip(nmmain.getZip());
        response.setBirthplace(nmmain.getBirthplace());
        response.setDrLic(nmmain.getDrLic());
        response.setDlState(nmmain.getDlState());
        response.setMarital(nmmain.getMarital());
        response.setOccupation(nmmain.getOccupation());
        response.setEye(nmmain.getEye());
        response.setHair(nmmain.getHair());
        response.setEmployer(nmmain.getEmployer());
        response.setHphone(nmmain.getHphone());
        response.setWphone(nmmain.getWphone());
        response.setMphone(nmmain.getMphone());
        response.setStreetNbr(nmmain.getStreetNbr());
        return response;
    }
}
