package models.classes;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Classe Ebean responsável por guardar a relacionamento entre uma WishList.class e Product.class
 */
@Entity
@Table(name = "wishlist_product")
@SequenceGenerator(name = WishList.SEQUENCE_NAME, sequenceName = WishList.SEQUENCE_NAME, initialValue = 1, allocationSize = 1)
public class WishListProduct extends Model {

    public static final String SEQUENCE_NAME = "wishlist_product_id_seq";

    public enum Status {
        ACTIVE, BLOCKED, REMOVED;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id")
    private WishList wishList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    public long getId() {
        return id;
    }

    public void setWishList(WishList wishList) {
        this.wishList = wishList;
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
