package models.classes;

import play.db.ebean.Model;

import javax.persistence.*;

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
}
