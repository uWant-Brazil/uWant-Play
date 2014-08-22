package models.classes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;
import utils.DateUtil;

import javax.persistence.*;
import java.util.Date;

/**
 * Classe Ebean responsável por guardar informações referentes aos comentários realizados por
 * usuários relacionados a ações compartilhadas/criadas.
 */
@Entity
@Table(name = "action_comments")
@SequenceGenerator(name = Comment.SEQUENCE_NAME, sequenceName = Comment.SEQUENCE_NAME, initialValue = 1, allocationSize = 17)
public class Comment extends Model {

    public static final String SEQUENCE_NAME = "action_comments_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @Column(nullable = false, updatable = false, length = 255)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Column(nullable = false, updatable = false)
    private Action action;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Column(nullable = false, updatable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Date since;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @JsonIgnore
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.DATE_HOUR_PATTERN)
    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }
}
