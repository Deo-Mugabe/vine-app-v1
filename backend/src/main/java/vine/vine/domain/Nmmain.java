package vine.vine.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name = "nmmain")
public class Nmmain {
    @Id
    @Column(name = "name_id")
    private Long nameId;

    @Column(name = "state_id")
    private String stateId;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "nametype")
    private String nameType;

    @Column(name = "alias_id")
    private String aliasId;

    @Column(name = "middlename")
    private String middlename;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "race")
    private String race;

    @Column(name = "sex")
    private String sex;

    @Column(name = "height")
    private String height;

    @Column(name = "weight")
    private String weight;

    @Column(name = "ssn")
    private String ssn;

    @Column(name = "streetnbr")
    private String streetNbr;

    @Column(name = "street")
    private String street;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip")
    private String zip;

    @Column(name = "birthplace")
    private String birthplace;

    @Column(name = "dr_lic")
    private String drLic;

    @Column(name = "dl_state")
    private String dlState;

    @Column(name = "marital")
    private String marital;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "eye")
    private String eye;

    @Column(name = "hair")
    private String hair;

    @Column(name = "employer")
    private String employer;

    @Column(name = "hphone")
    private String hphone;

    @Column(name = "wphone")
    private String wphone;

    @Column(name = "mphone")
    private String mphone;
}