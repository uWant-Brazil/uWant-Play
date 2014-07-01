package models.classes;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Classe Ebean responsável por guardar informações referentes a denúncias de ações realizadas
 * por usuários do sistema.
 */
@Entity
@Table(name = "actions_report")
@SequenceGenerator(name = ActionReport.SEQUENCE_NAME, sequenceName = ActionReport.SEQUENCE_NAME, initialValue = 1, allocationSize = 29)
public class ActionReport extends Model {

    public static final String SEQUENCE_NAME = "actions_report_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(nullable = false, updatable = false, unique = true)
    private Action action;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "actions_report_user",
            joinColumns = {@JoinColumn(name = "user_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "action_report_id", nullable = false, updatable = false)}
    )
    private List<User> users;

    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false, updatable = false, columnDefinition = "timestamp without time zone default now()")
    private Date since;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }
}
