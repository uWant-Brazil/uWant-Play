package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.format.Formats;
import play.db.ebean.Model;
import utils.ActionUtil;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Classe Ebean responsável por guardar informações referentes as ações tomadas por usuários.
 */
@Entity
@Table(name = "actions")
@SequenceGenerator(name = Action.SEQUENCE_NAME, sequenceName = Action.SEQUENCE_NAME, initialValue = 1, allocationSize = 37)
public class Action extends Model {

    public static final String SEQUENCE_NAME = "actions_id_seq";

    public enum Type {
        ADD_FRIENDS_CIRCLE,
        ACCEPT_FRIENDS_CIRCLE,
        COMMENT,
        MENTION,
        SHARE,
        WANT,
        REPORT,
        MESSAGE,
        ACTIVITY;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @Enumerated(value = EnumType.ORDINAL)
    @Column(nullable = false)
    private Type type;

    @OneToOne(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private User from;

    @OneToOne(fetch = FetchType.LAZY)
    @Column(nullable = false)
    private User user;

    @Column(nullable = true)
    private String extra;

    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @Version
    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
    private Date modifiedAt;

    @OneToMany(mappedBy = "action", fetch = FetchType.LAZY)
    private List<Comment> comments;

    @OneToOne(mappedBy = "action", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private WishList wishList;

    @JsonIgnore
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @JsonIgnore
    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @JsonIgnore
    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @JsonIgnore
    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @JsonIgnore
    public WishList getWishList() {
        return wishList;
    }

    public void setWishList(WishList wishList) {
        this.wishList = wishList;
    }

    @Override
    public String toString() {
        try {
            return ActionUtil.generateMessage(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
