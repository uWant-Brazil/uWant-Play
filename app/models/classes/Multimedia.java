package models.classes;

import models.cdn.CDNType;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Classe Ebean responsável por guardar informações referentes a arquivos multimídias guardados nas CDN's.
 */
@Entity
@Table(name = "multimedia")
@SequenceGenerator(name = Multimedia.SEQUENCE_NAME, sequenceName = Multimedia.SEQUENCE_NAME, initialValue = 1, allocationSize = 1)
public class Multimedia extends Model {

    public static final String SEQUENCE_NAME = "multimedia_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private long id;

    private String fileName;
    private String url;

    @Enumerated(EnumType.ORDINAL)
    private CDNType cdn;

    @Version
    private Date modifiedAt;

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

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
