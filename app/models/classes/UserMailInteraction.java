package models.classes;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Classe Ebean responsável por persistir informações da confirmação do e-mail de um novo usuário no sistema.
 */
@Entity
@Table(name = "user_mail_interaction")
@SequenceGenerator(name = UserMailInteraction.SEQUENCE_NAME, sequenceName = UserMailInteraction.SEQUENCE_NAME, initialValue = 1, allocationSize = 53)
public class UserMailInteraction extends Model {

    public static final String SEQUENCE_NAME = "user_mail_interaction_id_seq";

    public enum Status {
        DONE, WAITING, CANCELED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    /**
     * O HASH segue as normativas de criptografia - vide MailUtil.class
     */
    private String hash;

    /**
     * E-mail no qual foi enviado.
     */
    private String mail;

    /**
     * Usuário que necessita utilizar algum recurso com o e-mail.
     */
    @OneToOne
    private User user;

    @Version
    @Column(name = "modifiedAt")
    private Date modifiedAt;

    public UserMailInteraction() {
        // Do nothing...
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
