package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Cleibson Gomes on 21/05/14.
 * @See 1.0
 */
@Entity
@Table(name = "product")
@SequenceGenerator(name = Product.SEQUENCE_NAME, sequenceName = Product.SEQUENCE_NAME, initialValue = 1, allocationSize = 1)
public class Product extends Model{

    public static final String SEQUENCE_NAME = "product_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    private String name;
    private String nickName;

    @OneToOne
    @JoinColumn(name="id")
    private Manufacturer manufacturer;

    @OneToMany(mappedBy="product", fetch = FetchType.LAZY)
    private List<WishlistProduct> wishlistProducts;

    @OneToOne
    @JoinColumn(name = "multimedia")
    private Multimedia multimedia;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public List<WishlistProduct> getWishlistProducts() {
        return wishlistProducts;
    }

    public void setWishlistProducts(List<WishlistProduct> wishlistProducts) {
        this.wishlistProducts = wishlistProducts;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @JsonIgnore
    public Multimedia getMultimedia() {
        return multimedia;
    }

    public void setMultimedia(Multimedia multimedia) {
        this.multimedia = multimedia;
    }
}
