package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Classe Ebean responsável por guardar informações referentes ao dispositivo móvel no qual
 * o usuário do sistema está vinculado. Estes dados serão utilizados para envio de notificações pelo sistema.
 */
@Entity
@Table(name = "user_mobiles")
@SequenceGenerator(name = Manufacturer.SEQUENCE_NAME, sequenceName = Manufacturer.SEQUENCE_NAME, initialValue = 1, allocationSize = 33)
public class Mobile extends Model {

    private static final String SEQUENCE_NAME = "user_mobiles_id_seq";

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

    @Version
    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
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

    @JsonIgnore
    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
