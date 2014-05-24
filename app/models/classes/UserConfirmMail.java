package models.classes;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Hibernate Bean Model responsável por persistir informações da confirmação do e-mail de um novo usuário no sistema.
 */
@Entity
@Table(name = "user_confirm_mail")
@SequenceGenerator(name = UserConfirmMail.SEQUENCE_NAME, sequenceName = UserConfirmMail.SEQUENCE_NAME, initialValue = 1, allocationSize = 53)
public class UserConfirmMail extends Model {

    public static final String SEQUENCE_NAME = "user_id_seq";

    public enum Status {
        CONFIRMATED, WAITING_CONFIRMATION;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * O HASH segue as normativas de criptografia - vide MailUtil.class
     */
    private String hash;

    /**
     * E-mail no qual foi enviado a confirmação.
     */
    private String email;

    /**
     * Usuário que necessita confirmar seu e-mail antes de liberar o acesso total ao sistema.
     */
    @OneToOne
    private User user;

    public UserConfirmMail() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
