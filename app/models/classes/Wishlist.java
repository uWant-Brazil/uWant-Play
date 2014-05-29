package models.classes;

import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by Cleibson Gomes on 21/05/14.
 * @See 1.0
 */
@Entity
@Table(name = "wishlist")
@SequenceGenerator(name = Wishlist.SEQUENCE_NAME, sequenceName = Wishlist.SEQUENCE_NAME, initialValue = 1, allocationSize = 1)
public class Wishlist extends Model{

    public static final String SEQUENCE_NAME = "wishlist_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    private String title;
    private String description;

}
