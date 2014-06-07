package models.classes;

import models.cdn.CDNType;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by Cleibson Gomes on 27/05/14.
 * @see 1.1
 */
@Entity
@Table(name = "multimedia")
@SequenceGenerator(name = Manufacturer.SEQUENCE_NAME, sequenceName = Manufacturer.SEQUENCE_NAME, initialValue = 1, allocationSize = 1)
public class Multimedia extends Model {

    public static final String SEQUENCE_NAME = "multimedia_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    private String fileName;
    private String url;

    @Enumerated(EnumType.ORDINAL)
    private CDNType cdn;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public CDNType getCdn() {
        return cdn;
    }

    public void setCdn(CDNType cdn) {
        this.cdn = cdn;
    }
}
