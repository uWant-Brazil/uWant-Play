package models.classes;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Classe Ebean responsável por guardar informações dos tokens de acesso ao sistema.
 */
@Entity
@Table(name = "token")
@SequenceGenerator(name = Token.SEQUENCE_NAME, sequenceName = Token.SEQUENCE_NAME, initialValue = 1, allocationSize = 257)
public class Token extends Model {

    public static final String SEQUENCE_NAME = "token_id_seq";

    public enum Target {
        MOBILE, WEB;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(value = EnumType.ORDINAL)
    private Target target;

    @Version
    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
    private Date since;

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

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

}
