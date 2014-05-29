package models.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Calendar;

/**
 * Created by Cleibson Gomes on 21/05/14.
 * @See 1.0
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

}
