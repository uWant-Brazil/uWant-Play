package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Classe Ebean responsável por guardar informações referente ao produto que poderão ser adicionados a lista de desejos.
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
    @JoinColumn(name="manufacturer_id")
    private Manufacturer manufacturer;

    @OneToMany(mappedBy="product", fetch = FetchType.LAZY)
    private List<WishListProduct> wishListProducts;

    @OneToOne
    @JoinColumn(name = "multimedia")
    private Multimedia multimedia;

    @Version
    private Date modifiedAt;

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

    @JsonIgnore
    public List<WishListProduct> getWishListProducts() {
        return wishListProducts;
    }

    public void setWishListProducts(List<WishListProduct> wishListProducts) {
        this.wishListProducts = wishListProducts;
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

    public Multimedia getMultimedia() {
        return multimedia;
    }

    public void setMultimedia(Multimedia multimedia) {
        this.multimedia = multimedia;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
