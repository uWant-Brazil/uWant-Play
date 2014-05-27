package models.cdn;

/**
 * Created by Cleibson Gomes on 27/05/14.
 */
abstract class AbstractCDN {

    public enum Type {
        AMAZON_S3
    }

   private String host;
   private Type type;

    public AbstractCDN(String host, Type type) {
        this.host = host;
        this.type = type;
    }

    protected abstract String preparePassword();

    public String getHost() {
        return host;
    }

    public Type getType() {
        return type;
    }

}
