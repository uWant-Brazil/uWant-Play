package models.classes;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Created by felipebonezi on 21/05/14.
 */
@Entity
@Table(name = "users")
@SequenceGenerator(name = User.SEQUENCE_NAME, sequenceName = User.SEQUENCE_NAME, initialValue = 1, allocationSize = 11)
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
    private String firstName;
    private String middleName;
    private String lastName;
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

    public User() {
        // Do nothing...
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Calendar getBirthday() {
        return birthday;
    }

    public void setBirthday(Calendar birthday) {
        this.birthday = birthday;
    }

    public Calendar getSince() {
        return since;
    }

    public void setSince(Calendar since) {
        this.since = since;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public UserMailInteraction getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(UserMailInteraction confirmation) {
        this.confirmation = confirmation;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

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
    public Token getToken() {
        return this.token;
    }

    @Override
    public String getName() {
        return this.firstName.concat(" ").concat(this.lastName);
    }
}
