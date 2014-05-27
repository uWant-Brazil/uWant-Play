package models.cdn;

import models.classes.Multimedia;

/**
 * Created by infocusWeb2 on 27/05/14.
 */
public class AmazonS3CDN extends AbstractCDN implements ICDN {

    private static final String HOST = "";

    public AmazonS3CDN() {
        super(HOST, Type.AMAZON_S3);
    }

    @Override
    protected String preparePassword() {
        return null;
    }

    @Override
    public String getPassword() {
        return preparePassword();
    }

    @Override
    public void put(Multimedia multimedia) {
        String password = preparePassword();
    }

}
