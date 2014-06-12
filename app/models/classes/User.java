package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.List;

/**
 * Classe Ebean responsável por guardar informações referentes ao usuário do sistema.
 */
@Entity
@Table(name = "user")
@SequenceGenerator(name = User.SEQUENCE_NAME, sequenceName = User.SEQUENCE_NAME, initialValue = 1, allocationSize = 1)
public class User extends Model implements IMobileUser {

    public static final String SEQUENCE_NAME = "user_id_seq";

    public enum Gender {
        FEMALE, MALE;
    }

    public enum Status {
        ACTIVE, PARTIAL_ACTIVE, BLOCKED, REMOVED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    private String login;
    private String password;
    private String mail;
    private String name;
    private Calendar birthday;
    private Calendar since;

    @OneToOne(mappedBy = "user")
    private Token token;

    @OneToOne(mappedBy = "user")
    private UserMailInteraction confirmation;

    @Enumerated(EnumType.ORDINAL)
    private Gender gender;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    @Version
    private Calendar modifiedAt;

    @OneToMany(mappedBy="user", fetch = FetchType.LAZY)
    public List<WishList> wishList;

    public User() {
        // Do nothing...
    }

    @JsonIgnore
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public Calendar getBirthday() {
        return birthday;
    }

    public void setBirthday(Calendar birthday) {
        this.birthday = birthday;
    }

    @JsonIgnore
    public Calendar getSince() {
        return since;
    }

    public void setSince(Calendar since) {
        this.since = since;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    @JsonIgnore
    public UserMailInteraction getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(UserMailInteraction confirmation) {
        this.confirmation = confirmation;
    }

    @JsonIgnore
    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @JsonIgnore
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Calendar getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Calendar modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Override
    public String getLogin() {
        return this.login;
    }

    @Override
    @JsonIgnore
    public Token getToken() {
        return this.token;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @JsonIgnore
    public List<WishList> getWishList() {
        return wishList;
    }
}
