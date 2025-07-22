package vine.vine.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import vine.vine.service.Impl.ChargesServiceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private String dob;

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

    private static final Logger log = LoggerFactory.getLogger(Nmmain.class);

        // ✅ Helper method to safely convert string DOB to LocalDate
    public LocalDate getDobAsLocalDate() {
        if (dob == null || dob.trim().isEmpty()) {
            return null;
        }
        
        try {
            String cleanDob = dob.trim();
            
            // Skip obvious invalid values
            if (cleanDob.equalsIgnoreCase("null") || 
                cleanDob.equalsIgnoreCase("n/a") || 
                cleanDob.equals("00/00/0000") ||
                cleanDob.equals("0000-00-00")) {
                return null;
            }
            
            // Try different date formats your database might use
            if (cleanDob.length() == 8 && cleanDob.matches("\\d{8}")) {
                // YYYYMMDD format
                return LocalDate.parse(cleanDob, DateTimeFormatter.ofPattern("yyyyMMdd"));
            } else if (cleanDob.length() == 10 && cleanDob.contains("-")) {
                // YYYY-MM-DD format
                return LocalDate.parse(cleanDob);
            } else if (cleanDob.length() == 10 && cleanDob.contains("/")) {
                // MM/DD/YYYY format
                return LocalDate.parse(cleanDob, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            } else if (cleanDob.length() == 8 && cleanDob.contains("/")) {
                // M/D/YYYY or MM/D/YY format
                return LocalDate.parse(cleanDob, DateTimeFormatter.ofPattern("M/d/yyyy"));
            }
            
            log.debug("Unrecognized date format for DOB: '{}'", cleanDob);
            return null;
            
        } catch (Exception e) {
            log.debug("Could not parse DOB '{}': {}", dob, e.getMessage());
            return null;
        }
    }

    // ✅ Helper method to get formatted DOB string for output
    public String getFormattedDob(String pattern) {
        LocalDate localDate = getDobAsLocalDate();
        if (localDate == null) {
            return "";
        }
        
        try {
            return localDate.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            log.debug("Could not format DOB '{}' with pattern '{}': {}", dob, pattern, e.getMessage());
            return "";
        }
    }
}