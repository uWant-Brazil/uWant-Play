package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Cleibson Gomes on 03/06/14.
 *
 * @see 1.0
 */
@Entity
@Table(name = "wishlist_product")
@SequenceGenerator(name = Wishlist.SEQUENCE_NAME, sequenceName = Wishlist.SEQUENCE_NAME, initialValue = 1, allocationSize = 1)
public class WishlistProduct extends Model {

    public static final String SEQUENCE_NAME = "wishlist_product_id_seq";

    public enum Status {
        ACTIVE, BLOCKED, REMOVED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id")
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    public long getId() {
        return id;
    }

    @JsonIgnore
    public Wishlist getWishlist() {
        return wishlist;
    }

    public void setWishlist(Wishlist wishlist) {
        this.wishlist = wishlist;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
