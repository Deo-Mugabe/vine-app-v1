package vine.vine.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "archrg")
public class Charges {

    @Id
    @Column(name = "archrgid")
    private Long archrgid;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "arr_chrg")
    private String arr_chrg;

    @Column(name = "fel_misd")
    private String fel_misd;

    @Column(name = "chrg_cnt")
    private String chrg_cnt;

    @Column(name = "chrg_seq")
    private String chrg_seq;

    @Column(name = "bondamt")
    private String bondamt;

    @Column(name = "bondtype")
    private String bondtype;

    @Column(name = "armainid")
    private Long armainid;

    @Column(name = "chrgdesc")
    private String chrgdesc;

}
