package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Classe Ebean responsável por guardar informações referentes ao want do usuário.
 * OBS: Para saber quantos 'wants' uma ação possui, o ideal é realizar um 'Row Count'.
 */
@Entity
@Table(name = "action_comments_wants")
@SequenceGenerator(name = WantComment.SEQUENCE_NAME, sequenceName = WantComment.SEQUENCE_NAME, initialValue = 1, allocationSize = 3)
public class WantComment extends Model {

    public static final String SEQUENCE_NAME = "action_comments_wants_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Comment comment;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    @JsonIgnore
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonIgnore
    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
