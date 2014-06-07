package models.cdn;

import models.classes.Multimedia;

/**
 * Created by Cleibson Gomes on 27/05/14.
 */
abstract class AbstractCDN<K> {

   private String host;
   private CDNType type;

    public AbstractCDN(String host, CDNType type) {
        this.host = host;
        this.type = type;
    }

    protected abstract K prepareCredentials();

    protected Multimedia createMultimedia(String fileName, String url) {
        Multimedia multimedia = new Multimedia();
        multimedia.setFileName(fileName);
        multimedia.setUrl(url);
        multimedia.setCdn(this.type);
        multimedia.save();
        multimedia.refresh();
        return multimedia;
    }

    public String getHost() {
        return host;
    }

    public CDNType getType() {
        return type;
    }

}
