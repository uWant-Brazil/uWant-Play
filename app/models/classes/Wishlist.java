package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class WishList extends Model{

    public static final String SEQUENCE_NAME = "wishlist_id_seq";

    public enum Status {
        ACTIVE, BLOCKED, REMOVED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name="user_id")
    public User user;

    @ManyToMany(mappedBy = "wishlist", fetch = FetchType.LAZY)
    private List<WishListProduct> wishlists;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    @Version
    private Date modifiedAt;

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
        return wishlists;
    }

    public void setWishLists(List<WishListProduct> wishlists) {
        this.wishlists = wishlists;
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
}
