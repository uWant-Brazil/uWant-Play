package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Classe Ebean responsável por guardar informações referentes ao compartilhamento do usuário.
 * OBS: Para saber quantos 'shares' uma ação possui, o ideal é realizar um 'Row Count'.
 */
@Entity
@Table(name = "action_shares")
@SequenceGenerator(name = ActionShare.SEQUENCE_NAME, sequenceName = ActionShare.SEQUENCE_NAME, initialValue = 1, allocationSize = 3)
public class ActionShare extends Model {

    public static final String SEQUENCE_NAME = "action_shares_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Action action;

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
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
