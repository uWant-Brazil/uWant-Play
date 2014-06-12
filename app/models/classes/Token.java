package models.classes;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Classe Ebean responsável por guardar informações dos tokens de acesso ao sistema.
 */
@Entity
@Table(name = "token")
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Calendar getSince() {
        return since;
    }

    public void setSince(Calendar since) {
        this.since = since;
    }
}
