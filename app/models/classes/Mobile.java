package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Classe Ebean responsável por guardar informações referentes ao dispositivo móvel no qual
 * o usuário do sistema está vinculado. Estes dados serão utilizados para envio de notificações pelo sistema.
 */
@Entity
@Table(name = "user_mobiles")
@SequenceGenerator(name = Mobile.SEQUENCE_NAME, sequenceName = Mobile.SEQUENCE_NAME, initialValue = 1, allocationSize = 33)
public class Mobile extends Model {

    public static final String SEQUENCE_NAME = "user_mobiles_id_seq";

    public enum OS {
        ANDROID, IOS, WINDOWS_PHONE;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @Column(nullable = false, unique = true)
    private String identifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @Column(nullable = false, unique = true)
    private Token token;

    @Enumerated(value = EnumType.ORDINAL)
    private OS os;

    private Date since;

    @Version
    private Date modifiedAt;

    @JsonIgnore
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonIgnore
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public OS getOS() {
        return os;
    }

    public void setOS(OS os) {
        this.os = os;
    }

    @JsonIgnore
    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(OS os) {
        this.os = os;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
