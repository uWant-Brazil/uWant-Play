package models.classes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.format.Formats;
import play.db.ebean.Model;
import utils.DateUtil;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Classe Ebean responsável por guardar informações referentes ao usuário do sistema.
 */
@Entity
@Table(name = "users")
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.DATE_PATTERN)
    private Date birthday;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.DATE_HOUR_PATTERN)
    private Date since;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Token> tokens;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Mobile> mobiles;

    @Enumerated(EnumType.ORDINAL)
    private Gender gender;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    @Version
    private Date modifiedAt;

    @OneToMany(mappedBy="user", fetch = FetchType.LAZY)
    private List<WishList> wishList;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Multimedia picture;

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

    @JsonIgnore
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

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @JsonIgnore
    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

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

    @JsonIgnore
    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Override
    public String getLogin() {
        return this.login;
    }

    @Override
    @JsonIgnore
    public List<Token> getTokens() {
        return this.tokens;
    }

    @Override
    @JsonIgnore
    public List<Mobile> getMobiles() {
        return this.mobiles;
    }

    @Override
    public Multimedia getPicture() {
        return this.picture;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @JsonIgnore
    public List<WishList> getWishList() {
        return wishList;
    }

    public void setMobiles(List<Mobile> mobiles) {
        this.mobiles = mobiles;
    }

    public void setWishList(List<WishList> wishList) {
        this.wishList = wishList;
    }

    public void setPicture(Multimedia picture) {
        this.picture = picture;
    }
}
