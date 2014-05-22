package models.classes;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Created by felipebonezi on 21/05/14.
 */
@Entity
@Table(name = "tokens")
@SequenceGenerator(name = Token.SEQUENCE_NAME, sequenceName = Token.SEQUENCE_NAME, initialValue = 1, allocationSize = 257)
public class Token extends Model {

    public static final String SEQUENCE_NAME = "token_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private String content;

    @OneToOne
    private User user;

    @Version
    private Calendar since;

    public Token() {
        // Do nothing...
    }

}
