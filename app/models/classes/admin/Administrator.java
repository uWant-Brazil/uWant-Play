package models.classes.admin;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Subject;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

/**
 * Classe Ebean responsável por guardar informações referentes as ações tomadas por administradores.
 */
@Entity
@Table(name = "administrators")
@SequenceGenerator(name = Administrator.SEQUENCE_NAME, sequenceName = Administrator.SEQUENCE_NAME, initialValue = 1, allocationSize = 113)
public class Administrator extends Model implements Subject {

    public static final String SEQUENCE_NAME = "administrators_id_seq";
    public static final String IDENTIFIER = "subject.admin";

    public enum Status {
        ACTIVE, BLOCKED, REMOVED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @Column(nullable = false, unique = true)
    private String login;

    private String name;

    private String mail;

    private Status status;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "administrators_roles")
    private List<Role> roles;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public List<? extends be.objectify.deadbolt.core.models.Role> getRoles() {
        return this.roles;
    }

    @Override
    public List<? extends Permission> getPermissions() {
        return Collections.emptyList();
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

}
