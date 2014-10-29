package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Classe Ebean responsável por guardar informações referentes a fabricantes de produtos.
 */
@Entity
@Table(name = "manufacturer")
@SequenceGenerator(name = Manufacturer.SEQUENCE_NAME, sequenceName = Manufacturer.SEQUENCE_NAME, initialValue = 1, allocationSize = 1)
public class Manufacturer extends Model{

    public static final String SEQUENCE_NAME = "manufacturer_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    private String name;

    @OneToMany(mappedBy="manufacturer", fetch = FetchType.LAZY)
    public List<Product> products;

    @Version
    private Date modifiedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @JsonIgnore
    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
