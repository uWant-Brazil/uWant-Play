package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Classe Ebean responsável por guardar informações da lista de desejos.
 */
@Entity
@Table(name = "wishlist")
@SequenceGenerator(name = WishList.SEQUENCE_NAME, sequenceName = WishList.SEQUENCE_NAME, initialValue = 1, allocationSize = 1)
public class WishList extends Model {

    public static final String SEQUENCE_NAME = "wishlist_id_seq";

    public enum Status {
        ACTIVE, BLOCKED, REMOVED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @Column(name =  "uuid", nullable = false, unique = true)
    private String UUID;

    private String title;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    public User user;

    @OneToMany(mappedBy = "wishList", fetch = FetchType.LAZY)
    private List<WishListProduct> wishLists;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    @Version
    @Formats.DateTime(pattern="yyyy-MM-dd HH:mm:ss")
    private Date modifiedAt;

    @OneToOne(fetch = FetchType.LAZY)
    private Action action;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @JsonIgnore
    public List<WishListProduct> getWishLists() {
        return wishLists;
    }

    public void setWishLists(List<WishListProduct> wishlists) {
        this.wishLists = wishlists;
    }

    @JsonIgnore
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @JsonIgnore
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }
}
