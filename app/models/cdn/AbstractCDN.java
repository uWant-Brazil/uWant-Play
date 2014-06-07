package models.cdn;

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

    public String getHost() {
        return host;
    }

    public CDNType getType() {
        return type;
    }

}
